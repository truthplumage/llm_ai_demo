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

	// 단건 질의에 대한 동기 응답 전달
	public ChatResult chat(String message) {
		return chatModel.chat(new ChatMessage(message));
	}

	// SSE 스트리밍에 사용되는 토큰 Flux 반환
	public Flux<String> stream(String message) {
		return chatModel.stream(new ChatMessage(message));
	}
}
