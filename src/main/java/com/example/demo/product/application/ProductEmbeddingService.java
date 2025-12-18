package com.example.demo.product.application;

import com.example.demo.product.domain.Product;
import com.example.demo.product.infrastructure.PgVectorUtils;
import com.example.demo.product.infrastructure.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductEmbeddingService {

	private final EmbeddingModel embeddingModel;
	private final ProductRepository productRepository;

	public void index(Product product) {
		String text = "%s %s %s %s".formatted(
			product.name(),
			product.description(),
			"status:" + product.status(),
			"stock:" + product.stock());
		float[] embedding = embeddingModel.embed(text);
		SqlParameterValue vector = PgVectorUtils.toSqlVector(embedding);
		productRepository.updateEmbedding(product.id(), vector);
	}
}
