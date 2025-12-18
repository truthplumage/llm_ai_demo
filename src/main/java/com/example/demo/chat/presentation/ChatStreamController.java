package com.example.demo.chat.presentation;

import com.example.demo.chat.application.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "Chat", description = "LLM 챗봇 API")
public class ChatStreamController {
	private final ChatService chatService;

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "SSE 스트리밍 챗봇 응답")
	public Flux<ServerSentEvent<String>> stream(@RequestParam @NotBlank String message) {
		// SSE 형식으로 토큰을 지속 전송
		return chatService.stream(message)
			.map(token -> ServerSentEvent.builder(token).build());
	}
}
