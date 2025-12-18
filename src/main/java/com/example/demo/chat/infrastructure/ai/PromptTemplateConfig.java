package com.example.demo.chat.infrastructure.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.prompt.PromptTemplate;

@Configuration
public class PromptTemplateConfig {

	@Bean
	public PromptTemplate shoppingAssistantTemplate() {
		// 간결한 쇼핑 도우미 답변을 위한 기본 프롬프트 템플릿
		String template = """
		너는 쇼핑 도우미야. 간결하게 답변해.
		사용자 질문: {question}
		""";
		return new PromptTemplate(template);
	}
}
