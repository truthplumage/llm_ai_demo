package com.example.demo.product.application;

import com.example.demo.product.domain.ProductSearchResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductRagService {

	private final ProductSearchService productSearchService;
	private final ObjectProvider<ChatClient> chatClientProvider;

	public ProductAnswer answer(String question, int topK) {
		List<ProductSearchResult> contexts = productSearchService.search(question, topK);

		ChatClient chatClient = chatClientProvider.getIfAvailable();
		if (chatClient == null) {
			return new ProductAnswer("ChatClient가 설정되지 않았습니다.", contexts);
		}

		String contextText = contexts.stream()
			.map(c -> "- %s | %s | 가격:%s | 재고:%d".formatted(
				c.name(), c.description(), c.price(), c.stock()))
			.collect(Collectors.joining("\n"));

		if (contextText.isBlank()) {
			return new ProductAnswer("검색 결과가 없어 답변을 만들 수 없습니다.", contexts);
		}

		String template = """
		너는 상품 추천 도우미야. 아래 검색 결과만 근거로 간결하게 답변해.
		검색 결과:
		{context}

		사용자 질문: {question}
		""";
		Prompt prompt = new PromptTemplate(template).create(Map.of(
			"context", contextText,
			"question", question));

		ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
		Generation generation = response.getResult();
		return new ProductAnswer(generation.getOutput().getContent(), contexts);
	}

	public record ProductAnswer(String answer, List<ProductSearchResult> contexts) {}
}
