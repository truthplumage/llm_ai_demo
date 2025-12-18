package com.example.demo.chat.infrastructure.ai;

import com.example.demo.chat.domain.ChatMessage;
import com.example.demo.chat.domain.ChatModel;
import com.example.demo.chat.domain.ChatResult;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class SpringAiChatModel implements ChatModel {

	private final ObjectProvider<ChatClient> chatClientProvider;
	private final PromptTemplate shoppingAssistantTemplate;
	// application.yml 또는 환경변수로 전달된 OpenAI API 키
    @Value("${spring.ai.openai.api-key:}")
	private String apiKey;

	@Override
	public ChatResult chat(ChatMessage message) {
		// 키나 ChatClient가 준비되지 않은 경우 예외 대신 안내 메시지를 반환
		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null || apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			return new ChatResult("OpenAI API key not configured", Map.of("source", "fallback"));
		}
		// 쇼핑 도우미 템플릿에 질문을 주입하고 동기 호출 수행
		Prompt prompt = shoppingAssistantTemplate.create(Map.of("question", message.value()));
		ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
		Generation generation = response.getResult();
		Map<String, Object> metadata = new HashMap<>();
		response.getMetadata().entrySet()
			.forEach(entry -> metadata.put(entry.getKey(), entry.getValue()));
		return new ChatResult(generation.getOutput().getContent(), metadata);
	}

	@Override
	public Flux<String> stream(ChatMessage message) {
		// 스트리밍도 동일하게 키/빈 검사를 거쳐 SSE용 토큰 Flux 반환
		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null || apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			return Flux.just("OpenAI API key not configured");
		}
		Prompt prompt = shoppingAssistantTemplate.create(Map.of("question", message.value()));
		return chatClient.prompt(prompt)
			.stream()
			.content();
	}
}
