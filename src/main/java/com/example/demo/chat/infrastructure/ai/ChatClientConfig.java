package com.example.demo.chat.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

	// Spring AI가 자동 주입하는 Builder로 실제 ChatClient 빈을 생성해
	// ObjectProvider.getIfAvailable() 호출 시 null을 방지한다.
	@Bean
	public ChatClient chatClient(ChatClient.Builder builder) {
		return builder.build();
	}
}
