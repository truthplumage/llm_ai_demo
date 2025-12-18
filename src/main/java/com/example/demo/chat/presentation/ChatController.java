package com.example.demo.chat.presentation;

import com.example.demo.chat.application.ChatService;
import com.example.demo.chat.domain.ChatResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "LLM 챗봇 API")
public class ChatController {
    // 환경에서 주입된 API 키를 확인용으로 로깅에 사용
    @Value("${spring.ai.openai.api-key}")
    private String openAiKey;
	private final ChatService chatService;

	@PostMapping
	@Operation(summary = "단건 챗봇 응답")
	public ChatReply chat(@Valid @RequestBody ChatRequest request) {
        // 동기 방식으로 단일 질문에 대한 답변을 생성
        log.info(" openAiKey: {}", openAiKey);
		ChatResult result = chatService.chat(request.message());
		return new ChatReply(result.content(), result.metadata());
	}

	public record ChatRequest(@NotBlank String message) {
	}

	public record ChatReply(String content, Map<String, Object> metadata) {
	}
}
