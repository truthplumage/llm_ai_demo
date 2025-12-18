package com.example.demo.chat.presentation;

import com.example.demo.chat.application.ChatService;
import com.example.demo.chat.domain.ChatResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@PostMapping
	public ChatReply chat(@Valid @RequestBody ChatRequest request) {
		ChatResult result = chatService.chat(request.message());
		return new ChatReply(result.content(), result.metadata());
	}

	public record ChatRequest(@NotBlank String message) {
	}

	public record ChatReply(String content, Map<String, Object> metadata) {
	}
}
