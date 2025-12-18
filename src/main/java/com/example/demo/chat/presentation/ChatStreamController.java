package com.example.demo.chat.presentation;

import com.example.demo.chat.application.ChatService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
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
public class ChatStreamController {

	private final ChatService chatService;

	@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> stream(@RequestParam @NotBlank String message) {
		return chatService.stream(message)
			.map(token -> ServerSentEvent.builder(token).build());
	}
}
