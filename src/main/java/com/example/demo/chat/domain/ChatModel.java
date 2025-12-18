package com.example.demo.chat.domain;

import reactor.core.publisher.Flux;

public interface ChatModel {

	ChatResult chat(ChatMessage message);

	Flux<String> stream(ChatMessage message);
}
