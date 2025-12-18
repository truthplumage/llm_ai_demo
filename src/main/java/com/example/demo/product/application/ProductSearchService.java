package com.example.demo.product.application;

import com.example.demo.product.domain.ProductSearchResult;
import com.example.demo.product.infrastructure.PgVectorUtils;
import com.example.demo.product.infrastructure.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

	private final EmbeddingModel embeddingModel;
	private final ProductRepository productRepository;

	public List<ProductSearchResult> search(String query, int topK) {
		float[] queryVector = embeddingModel.embed(query);
		return productRepository.searchSimilar(PgVectorUtils.toSqlVector(queryVector), topK);
	}
}
