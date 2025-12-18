# [실습2] RAG 기반 추천 기능 구현

## 목표
- 장바구니 데이터를 활용해 벡터 검색 + LLM을 결합한 추천 파이프라인을 구축한다.
- 결과: 유저 장바구니 → 검색용 키워드 추출 → 코사인 유사도 상위 상품 3개를 JSON으로 반환.

## 아키텍처 개요
1) 상품 문서 임베딩 생성 → Chroma 컬렉션 업서트
2) 유저 장바구니 JSON을 프롬프트로 전달해 검색용 `queryTerm` 생성 (구조화 출력)
3) `queryTerm` 임베딩 → Chroma k-NN 검색 → 상위 3개 상품 반환
4) 검색 결과를 응답하거나, 추가로 LLM 포맷팅/랭킹 수행

## 사전 준비
- 임베딩/챗 모델: OpenAI 임베딩(`text-embedding-3-small/large`) + Chat 모델(`gpt-4o-mini` 등) 키 설정.
- 의존성 예시:
```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
    implementation 'io.milvus:milvus-sdk-java:2.4.2' // 또는 Chroma용 HTTP/GRPC 클라이언트 라이브러리
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
}
```

## 데이터 준비 및 임베딩 적재
1. **데이터 정규화**: 상품 소개문, 카테고리, 가격, 재고 상태를 CSV/JSON으로 정리.
2. **청크 분할**: 200~500자 단위 문장으로 분할. 중복을 줄이기 위해 10~20% 중첩 슬라이딩 윈도우.
3. **임베딩 생성**: 동일 모델로 모든 청크를 임베딩.
4. **업서트**: `id`, `embedding`, `metadata`(`productId`, `category`, `price`, `brand`, `stock`)와 함께 컬렉션에 저장.

예시 의사코드 (Chroma REST 가정):
```java
List<String> ids = ...; // 청크별 ID
List<float[]> embeddings = embeddingModel.embed(chunks);
List<Map<String, Object>> metadatas = ...; // productId, category 등
chromaClient.upsert("products", ids, embeddings, metadatas, chunks);
```

## 장바구니 컨텍스트 → 검색어 추출 프롬프트
```java
String template = """
너는 쇼핑 추천 엔진이다.
입력된 장바구니를 보고 사용자가 관심 있을 상품 유형을 한 줄로 요약해라.
다음 JSON 스키마를 반드시 지켜라.
{
  "reason": "요약 이유",
  "queryTerm": "검색용 키워드"
}
장바구니: {cartJson}
""";

PromptTemplate promptTemplate = new PromptTemplate(template);
String cartJson = objectMapper.writeValueAsString(cartDto);
Prompt prompt = promptTemplate.create(Map.of("cartJson", cartJson));
var result = chatClient.generate(prompt).getResult();
QueryIntent intent = objectMapper.readValue(result.getOutput().getContent(), QueryIntent.class);
```
- `QueryIntent`에는 `reason`, `queryTerm`를 매핑. JSON 파싱 실패 시 재시도/백업 프롬프트 사용.

## 쿼리 벡터화 및 검색
```java
float[] queryEmbedding = embeddingModel.embed(intent.queryTerm());
List<SearchResult> hits = chromaClient.query(
    collectionName: "products",
    queryEmbedding,
    k: 3,
    where: Map.of("stock", true) // 품절 제외
);
```
- 반환된 `hits`에 `documents`, `metadatas`, `scores` 포함. 스코어 임계값(예: 0.75) 미만은 필터.

## 응답 포맷팅
- 단순 응답: 상위 3개 상품의 `{name, category, price, description}` JSON 배열을 API 응답으로 직접 반환.
- LLM 재포맷팅(선택): 근거를 포함해 설명하도록 LLM에 프롬프트.
```java
String formatPrompt = """
다음 상품 후보를 보고 사용자에게 3개를 추천해라.
각 항목은 name, category, price, reason 필드를 가진 JSON 배열로 반환해라.
후보: {products}
""";
Prompt p = new PromptTemplate(formatPrompt).create(Map.of("products", hitsJson));
String finalAnswer = chatClient.generate(p).getResult().getOutput().getContent();
```

## 테스트 시나리오
- 장바구니에 전자책 리더기 액세서리 → `queryTerm`이 "전자책 리더기 케이스" 등으로 추출되는지 확인.
- 가격/카테고리 메타 필터가 적용되어 의도한 범위로만 검색되는지 확인.
- JSON 스키마 불일치/파싱 실패 시 재시도 로직 동작 여부 확인.
- 검색 스코어 임계값 미달 시 "추천할 근거 없음" 처리 확인.

## 운영 팁
- 임베딩/챗 모델 버전을 프로퍼티로 분리해 롤백 가능하게 유지.
- 토큰/임베딩 비용 모니터링: 응답에 `usage` 포함, 로그/메트릭 기록.
- 품질 개선: 재랭킹(Cross-encoder), 다중 샘플 self-consistency, 사용자 피드백 루프 추가.
