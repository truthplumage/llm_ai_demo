package com.example.demo.product.infrastructure;

import com.example.demo.product.domain.ProductSearchResult;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

	private final JdbcTemplate jdbcTemplate;

	public void updateEmbedding(UUID productId, SqlParameterValue vector) {
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement("UPDATE product SET embedding = ? WHERE id = ?");
			ps.setObject(1, vector.getValue(), vector.getSqlType());
			ps.setObject(2, productId);
			return ps;
		});
	}

	public List<ProductSearchResult> searchSimilar(SqlParameterValue queryVector, int topK) {
		return jdbcTemplate.query(
			"""
			SELECT id, name, description, price, stock, status, embedding <=> ? AS score
			FROM product
			ORDER BY embedding <=> ?
			LIMIT ?
			""",
			ps -> {
				ps.setObject(1, queryVector.getValue(), queryVector.getSqlType());
				ps.setObject(2, queryVector.getValue(), queryVector.getSqlType());
				ps.setInt(3, topK);
			},
			(rs, rowNum) -> new ProductSearchResult(
				rs.getObject("id", UUID.class),
				rs.getString("name"),
				rs.getString("description"),
				rs.getBigDecimal("price"),
				rs.getInt("stock"),
				rs.getString("status"),
				rs.getDouble("score"))
		);
	}
}
