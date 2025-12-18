# 쇼핑 도우미 현재 상태 및 개선 아이디어

## 현재 동작
- 내부 DB 연동 없음. `SpringAiChatModel`이 프롬프트에 사용자 질문만 넣어 OpenAI로 호출하고 응답을 그대로 반환.
- 따라서 상품 정보를 조회하거나 확인할 수 없으며, DB에 무엇이 있는지 모델이 모른다.

## 내부 상품 DB를 활용하려면
- **RAG/벡터 검색** (권장): 상품 텍스트(이름/설명/카테고리/가격 등)를 임베딩해 벡터 DB에 저장하고, 질의 시 유사한 상품을 검색해 컨텍스트로 넣는다.
  - **인덱싱 파이프라인**: DB에서 상품을 읽어 청크 단위(예: 문장/문단)로 분리 → 임베딩 생성 → `vectorstore`(예: pgvector, Milvus, Pinecone 등)에 저장. 메타데이터에 `productId`, `가격`, `카테고리`, `업데이트 시각` 등을 포함.
  - **질의 흐름**: 사용자가 질문 → 질의 텍스트 임베딩 생성 → 벡터 검색(top-k, 필터로 카테고리/가격대 제한) → 검색된 문서/필드를 프롬프트에 삽입 → LLM 호출 → 응답 반환.
  - **프롬프트 예시**: “아래 상품 정보만 활용해 답하라. 정보에 없는 내용은 모른다고 말하라.” + 검색된 상품의 이름/가격/설명 요약을 리스트로 포함.
  - **신선도 관리**: 상품이 변경될 때마다 인덱싱 배치/이벤트를 돌리거나, 업데이트 로그 기반으로 해당 상품만 재임베딩.
  - **도구/라이브러리**: Spring AI의 `vectorstore` 모듈 + `EmbeddingModel`(OpenAI 등) 활용. 로컬 DB만 사용한다면 pgvector를 붙이는 것이 간단.

## 설정/운영 팁
- `.env` 자동 로드: `spring.config.import: optional:file:.env[.properties]`가 `application.yml`에 설정되어 있으므로 프로젝트 루트에서 실행하면 `OPENAI_API_KEY`를 읽는다.
- 모델 설정: `gpt-4o-mini`는 OpenAI가 제공하는 경량 4o 모델. `max-tokens`를 늘리면 답변이 길어지지만 비용과 지연이 증가한다.

## pgvector 연동 예시 (문서용)
- **의존성**: `org.springframework.ai:spring-ai-pgvector-store-spring-boot-starter`(또는 JDBC/R2DBC로 직접 쿼리) 추가가 필요.
- **확장/스키마**
  ```sql
  CREATE EXTENSION IF NOT EXISTS vector;
  ALTER TABLE product ADD COLUMN IF NOT EXISTS embedding vector(1536);
  CREATE INDEX IF NOT EXISTS product_embedding_idx
    ON product USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
  ```
- **인덱싱 (임베딩 생성 후 upsert)**
  ```java
  @Service
  @RequiredArgsConstructor
  class ProductEmbeddingSync {
      private final EmbeddingModel embeddingModel;
      private final JdbcTemplate jdbcTemplate;

      public void index(Product p) {
          String text = "%s %s status:%s stock:%s".formatted(p.getName(), p.getDescription(), p.getStatus(), p.getStock());
          float[] vec = embeddingModel.embed(text);
          jdbcTemplate.update("UPDATE product SET embedding = ? WHERE id = ?", toPgVector(vec), p.getId());
      }

      private Object toPgVector(float[] v) {
          return new SqlParameterValue(Types.OTHER, Arrays.toString(v)); // '[...]'
      }
  }
  ```
- **질의 시 검색**
  ```sql
  -- 질의 임베딩을 `:qvec`로 전달한다고 가정
  SELECT id, name, price, description
  FROM product
  ORDER BY embedding <=> :qvec
  LIMIT 5;
  ```
- **프롬프트 주입**: 검색 결과의 이름/가격/설명을 리스트로 만들어 “아래 정보만 활용해 답하라” 같은 시스템 메시지에 포함시킨 뒤 LLM 호출.
- **Spring AI 임베딩 반환 타입**: 1.0.0-M4 기준 `EmbeddingModel.embed(String)`은 `float[]`를 반환하므로 pgvector 파라미터로 넘길 때는 `Arrays.toString()` 형태(`[0.1, 0.2, ...]`)로 문자열화해 `SqlParameterValue(Types.OTHER, ...)`로 전달한다.
