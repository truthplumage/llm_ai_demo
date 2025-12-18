# LLM 기반 추천 기능 커리큘럼

## [이론1] LLM의 기본 개념
- LLM은 대규모 텍스트 코퍼스를 기반으로 사전학습된 언어 모델로, 입력 토큰을 맥락으로 다음 토큰을 예측하며 생성·요약·분류 등 다양한 작업을 수행합니다.
- 대표 LLM API 서비스: OpenAI(GPT 계열), Azure OpenAI, Google(Gemini), AWS Bedrock, Anthropic(Claude) 등으로, REST/SDK를 통해 모델 선택·토큰 제한·가격 정책이 결정됩니다.
- OpenAI Chat Completions 기본 요청/응답: `model`, `messages`(system/user/assistant 역할), `temperature/top_p/max_tokens/stream` 옵션을 포함하며, 응답은 `choices[].message`에 생성 텍스트, `usage`에 토큰 통계가 반환됩니다.

## [이론2] Spring AI를 이용한 LLM API 호출
- Spring AI는 OpenAI·Azure·HuggingFace 등 다양한 프로바이더를 추상화하여 `ChatClient` 혹은 `StreamingChatClient`로 간단히 호출할 수 있게 합니다.
- 응답 스트리밍은 `stream()`을 통해 SSE/Flux로 청크 단위 토큰을 받아 webflux 컨트롤러에서 바로 전송할 수 있어 체감 지연을 줄입니다.
- 프롬프트 템플릿은 플레이스홀더 기반(`PromptTemplate`, `Message`)으로 시스템 지침과 사용자 입력을 조합해 일관된 프롬프트를 구성합니다.

## [실습1] Spring AI를 이용한 LLM API 호출
- 목표: Spring Boot + Spring AI로 간단한 챗봇 API를 만들고, 스트리밍과 프롬프트 템플릿을 적용합니다.
- 설정: `spring-ai-openai-spring-boot-starter` 의존성 추가, `spring.ai.openai.api-key` 환경변수 설정.
- 예시 흐름
  - `ChatClient` 빈 구성: `@Bean ChatClient chatClient(OpenAiChatModel model)`
  - REST 컨트롤러: `/chat` POST로 user 메시지 수신 → `chatClient.generate(userMessage)` 호출 → 응답 반환.
  - 스트리밍: `/chat/stream` endpoint에서 `StreamingChatClient.stream()` → `Flux<ServerSentEvent<String>>`로 전달.
  - 프롬프트 템플릿: `"너는 쇼핑 도우미야. 사용자 입력: {input}"` 형태로 템플릿 생성 후 `Map.of("input", userMessage)`로 치환.

## [이론3] 할루시네이션과 Context
- 할루시네이션은 모델이 사실과 다른 내용을 그럴듯하게 생성하는 현상으로, 훈련 데이터 범위 밖 질의·불충분한 컨텍스트·과도한 온도 등이 원인입니다.
- 완화 전략: 신뢰 가능한 데이터로 프롬프트 보강(RAG), 시스템 메시지에 역할·제약 명시, 구조화된 출력(JSON/스키마) 요구, 근거를 요구하거나 근거 미제공 시 "모른다" 응답하도록 지시.
- 추가로 온도/토큰 제한 조정, 후처리 검증(스키마 검증·패턴 검사)도 활용합니다.

## [이론4] 벡터 검색
- 문서 벡터화: 문장/문서 임베딩 모델로 텍스트를 고차원 실수 벡터로 변환해 의미적 유사도를 수치화합니다.
- 코사인 유사도는 두 벡터 사이 각도의 cos 값을 의미하며 1에 가까울수록 의미가 비슷함을 나타냅니다.
- 벡터 DB(Chroma 등)는 벡터를 저장하고 ANN(근사 최근접) 검색으로 대규모 데이터에서도 빠른 유사 검색을 제공합니다.
- 구현 절차: (1) 원문 저장 + 메타데이터 관리 (2) 임베딩 생성 후 DB 업서트 (3) 질의 벡터화 (4) 유사도 상위 k개를 조회해 컨텍스트로 LLM에 전달.

## [실습2] RAG 기반 추천 기능 구현
- 목표: 장바구니 정보를 활용해 유사 상품을 추천하는 RAG 파이프라인을 구축합니다.
- 준비: 상품 소개/정보를 정규화(JSON/CSV) 후 문장 단위로 임베딩 생성 → Chroma 컬렉션에 `productId`, `category`, `price` 등 메타데이터와 함께 저장.
- 장바구니 컨텍스트 구성: 특정 유저의 장바구니를 JSON 문자열로 변환하고, 최근 구매/조회 이력 등 추가 속성을 포함해 프롬프트에 전달.
- 추천 질의 프롬프트 예시
  - 시스템: "너는 쇼핑 추천 엔진이다. 근거가 없으면 모른다고 답해라. JSON 스키마를 따르라."
  - 사용자: "장바구니: {cartJson}. 이 유저에게 추천할 상품 유형을 한 줄 요약으로 반환해줘. 필드는 `reason`, `queryTerm` 두 개만 포함한 JSON을 출력해." (구조화된 출력 요구)
- 검색 및 응답 생성
  - `queryTerm`을 임베딩하여 Chroma에서 코사인 유사도 상위 3개 상품 검색.
  - 검색 결과를 `{name, category, price, description}` JSON 배열로 묶어 LLM에 "다음 상품 중 3개를 JSON으로 반환" 지시 또는 바로 API 응답으로 전달.
- 확장 아이디어: 가격대 필터링, 품절 제외, 다국어 질의 지원, 재현성 확보용 seed/temperature 조절.
