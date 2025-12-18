package com.example.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
	info = @Info(
		title = "Demo Chat API",
		description = "Spring AI 기반 챗봇 REST/SSE API",
		version = "v1"
	)
)
// Swagger UI 노출과 스펙 기본 정보를 위한 OpenAPI 메타 설정
public class OpenApiConfig {
}
