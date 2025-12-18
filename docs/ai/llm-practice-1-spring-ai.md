# [실습1] Spring AI로 LLM API 호출 및 챗봇 구현

## 목표
- Spring Boot + Spring AI를 이용해 기본 챗봇 REST/SSE API를 구현하고, 프롬프트 템플릿과 스트리밍 응답을 적용한다.

## 사전 준비
- JDK 17 이상, Gradle 사용 프로젝트.
- OpenAI API 키를 환경변수 `OPENAI_API_KEY`로 설정.
- 의존성 추가 (build.gradle):
```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-openai-spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-webflux' // SSE 스트리밍
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```

## 설정 (application.yaml 예시)
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
          max-tokens: 256
```
- 로컬/운영 분리: `application-local.yaml`에서 개발 모델, `application-prod.yaml`에서 상위 모델 사용.

## 프롬프트 템플릿 정의
```java
@Configuration
public class PromptTemplates {
    public PromptTemplate shoppingAssistantTemplate() {
        String template = "너는 쇼핑 도우미야. 간결히 답해. 사용자 질문: {question}";
        return new PromptTemplate(template);
    }
}
```
- 시스템 지침·출력 포맷을 템플릿에 포함하고, 사용자 입력만 플레이스홀더로 치환.

## ChatClient 기반 단건 응답 API
```java
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatClient chatClient;
    private final PromptTemplate shoppingAssistantTemplate;

    @PostMapping("/api/chat")
    public ChatReply chat(@Valid @RequestBody ChatRequest req) {
        Prompt prompt = shoppingAssistantTemplate.create(Map.of("question", req.message()));
        var result = chatClient.generate(prompt).getResult();
        return new ChatReply(result.getOutput().getContent(), result.getMetadata());
    }
}

record ChatRequest(@NotBlank String message) {}
record ChatReply(String content, Map<String, Object> metadata) {}
```
- `metadata`에는 finish_reason, token usage 등이 포함될 수 있으므로 응답 품질/비용 모니터링에 활용.

## StreamingChatClient + WebFlux SSE
```java
@RestController
@RequiredArgsConstructor
public class ChatStreamController {
    private final StreamingChatClient streamingChatClient;
    private final PromptTemplate shoppingAssistantTemplate;

    @GetMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream(@RequestParam String message) {
        Prompt prompt = shoppingAssistantTemplate.create(Map.of("question", message));
        return streamingChatClient.stream(prompt)
            .map(resp -> resp.getResult().getOutput().getContent())
            .map(token -> ServerSentEvent.builder(token).build());
    }
}
```
- 프론트엔드에서는 EventSource/Fetch(stream)로 청크를 받아 점진적으로 표시.
- 긴 응답을 빠르게 체감하게 하고, 네트워크 단절 시 재연결을 고려해 클라이언트에서 누적 버퍼를 유지.

## 예외/시간제한 처리
- WebClient 커넥션/응답 타임아웃 설정, Circuit Breaker로 보호.
- 입력 길이 검사 및 `max_tokens` 제한.
- 429/5xx 응답에 대한 제한적 재시도(지수 백오프) 적용.

## 로깅/관측
- PII 마스킹 후 프롬프트/응답, latency, usage 토큰을 로그/메트릭으로 기록.
- 스트리밍의 경우 청크 수와 완료 이벤트(`finish_reason`)를 추적해 중단 여부를 확인.

## 빠른 수동 테스트
- 단건: `curl -X POST http://localhost:8080/api/chat -H "Content-Type: application/json" -d '{"message":"무선 이어폰 추천"}'`
- 스트리밍: `curl http://localhost:8080/api/chat/stream?message=전자책`

## 확장 아이디어
- 함수 호출을 사용해 상품 조회 API와 연동.
- 시스템 메시지에 브랜드 톤/금지사항을 추가.
- 다국어 입력을 위한 언어 감지 후 번역 파이프라인 구성.
