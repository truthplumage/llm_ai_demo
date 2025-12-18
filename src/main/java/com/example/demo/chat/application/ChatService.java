package com.example.demo.chat.application;

import com.example.demo.chat.domain.ChatMessage;
import com.example.demo.chat.domain.ChatModel;
import com.example.demo.chat.domain.ChatResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatModel chatModel;

	public ChatResult chat(String message) {
		return chatModel.chat(new ChatMessage(message));
	}

	public Flux<String> stream(String message) {
		return chatModel.stream(new ChatMessage(message));
	}
}
