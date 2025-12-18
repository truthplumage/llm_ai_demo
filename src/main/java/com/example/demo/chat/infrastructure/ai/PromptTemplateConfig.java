package com.example.demo.chat.infrastructure.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.prompt.PromptTemplate;

@Configuration
public class PromptTemplateConfig {

	@Bean
	public PromptTemplate shoppingAssistantTemplate() {
		String template = """
		너는 쇼핑 도우미야. 간결하게 답변해.
		사용자 질문: {question}
		""";
		return new PromptTemplate(template);
	}
}
