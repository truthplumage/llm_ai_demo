# [이론2] Spring AI를 이용한 LLM API 호출

## 개요
- Spring AI는 OpenAI·Azure·Bedrock·HuggingFace 등 다양한 LLM 제공자를 위한 공통 추상화를 제공하며, `ChatClient`, `StreamingChatClient`, `PromptTemplate` 등을 통해 일관된 호출 경험을 제공합니다.
- Spring Boot 스타터로 의존성을 추가하면 자동구성이 활성화되어 최소 설정으로 동작합니다.

## 기본 구성 요소
- **ChatClient**: 동기/비동기 텍스트 생성 클라이언트. `chatClient.generate()`로 단일 응답을 받음.
- **StreamingChatClient**: 스트리밍 생성 클라이언트. `stream(prompt)` → `Flux<ChatResponse>`로 토큰 청크 수신.
- **PromptTemplate**: 템플릿 문자열에 플레이스홀더를 두고 Map/객체로 치환하여 일관된 프롬프트를 생성.
- **Model beans**: 예) `OpenAiChatModel`, `OpenAiEmbeddingModel` 등이 프로바이더별로 구성됨.

## 의존성 예시 (Gradle)
```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
    // 스트리밍 SSE를 위한 WebFlux
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
}
```

## 설정 예시
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://api.openai.com/v1
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.6
```
- 환경변수/Secret Manager로 키를 주입하고, 프로퍼티로 기본 모델·샘플링 옵션을 정의합니다.

## ChatClient 호출 흐름
```java
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatClient chatClient;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest req) {
        PromptTemplate template = new PromptTemplate("너는 쇼핑 도우미야. 사용자: {q}");
        Prompt prompt = template.create(Map.of("q", req.message()));
        return chatClient.generate(prompt).getResult().getOutput().getContent();
    }
}
```
- `Prompt`는 `Message` 리스트(system/user/assistant)를 포함할 수도 있으며, 다중 메시지로 맥락을 전달합니다.

## 스트리밍과 WebFlux
- `StreamingChatClient.stream(prompt)`는 `Flux<ChatResponse>`를 반환하며, 각 `ChatResponse`는 delta 토큰을 포함.
- WebFlux에서 SSE로 전달 예시:
```java
@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> stream(@RequestParam String q) {
    Prompt prompt = new PromptTemplate("간결히 답해: {q}").create(Map.of("q", q));
    return streamingChatClient.stream(prompt)
        .map(resp -> resp.getResult().getOutput().getContent())
        .map(chunk -> ServerSentEvent.builder(chunk).build());
}
```
- 스트리밍 장점: 초기 바이트를 빠르게 전달해 사용자 체감 지연 감소, 긴 응답을 끊어서 표시 가능.

## 프롬프트 템플릿 설계
- 시스템 메시지에서 역할·톤·금지사항·출력 스키마를 명확히 명시.
- 사용자 입력은 플레이스홀더로 분리해 재사용성/보안을 확보.
- 예: `"너는 쇼핑 추천 엔진이다. 장바구니: {cart}. JSON 스키마: {schema}."`

## 예외 및 회복 전략
- **타임아웃**: WebClient의 read/write timeout 설정, Circuit Breaker(Resilience4j)로 보호.
- **재시도**: idempotent한 프롬프트에 한해 제한적 재시도; `429/500` 응답 시 백오프.
- **비용 가드**: `maxTokens`, `stop` 시퀀스, 응답 길이 제한, 입력 길이 사전 점검.
- **로깅/관측성**: PII 마스킹 후 프롬프트/응답, latency, usage 토큰 기록. 샘플링 로깅으로 비용 최소화.

## 테스트 포인트
- 템플릿 치환 결과 검증 (필수 필드 누락 방지).
- 응답 파싱 및 JSON 스키마 검증.
- 스트리밍 시 순서 보장/버퍼 크기 조정 확인.
- 장애 주입(타임아웃, 429) 후 Circuit Breaker/재시도 동작 확인.
