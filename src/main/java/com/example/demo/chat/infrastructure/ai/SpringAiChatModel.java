package com.example.demo.chat.infrastructure.ai;

import com.example.demo.chat.domain.ChatMessage;
import com.example.demo.chat.domain.ChatModel;
import com.example.demo.chat.domain.ChatResult;
import java.util.HashMap;
import java.util.Map;
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
public class SpringAiChatModel implements ChatModel {

	private final ObjectProvider<ChatClient> chatClientProvider;
	private final PromptTemplate shoppingAssistantTemplate;
	private final String apiKey;

	public SpringAiChatModel(ObjectProvider<ChatClient> chatClientProvider,
						 PromptTemplate shoppingAssistantTemplate,
						 @Value("${spring.ai.openai.api-key:}") String apiKey) {
		this.chatClientProvider = chatClientProvider;
		this.shoppingAssistantTemplate = shoppingAssistantTemplate;
		this.apiKey = apiKey;
	}

	@Override
	public ChatResult chat(ChatMessage message) {
		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null || apiKey == null || apiKey.isBlank() || "changeme".equals(apiKey)) {
			return new ChatResult("OpenAI API key not configured", Map.of("source", "fallback"));
		}
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
