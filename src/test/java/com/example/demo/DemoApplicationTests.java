package com.example.demo;

import static reactor.core.publisher.Flux.just;

import com.example.demo.chat.domain.ChatMessage;
import com.example.demo.chat.domain.ChatModel;
import com.example.demo.chat.domain.ChatResult;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

@SpringBootTest
@Import(DemoApplicationTests.TestChatConfig.class)
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class TestChatConfig {

		@Bean
		@Primary
		ChatModel testChatModel() {
			return new ChatModel() {
				@Override
				public ChatResult chat(ChatMessage message) {
					return new ChatResult("test", Map.of("source", "stub"));
				}

				@Override
				public Flux<String> stream(ChatMessage message) {
					return just("test-stream");
				}
			};
		}
	}

}
