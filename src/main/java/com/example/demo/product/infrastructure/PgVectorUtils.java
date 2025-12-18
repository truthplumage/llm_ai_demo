package com.example.demo.product.infrastructure;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import org.springframework.jdbc.core.SqlParameterValue;

public final class PgVectorUtils {

	private PgVectorUtils() {
	}

	public static SqlParameterValue toSqlVector(List<Double> embedding) {
		return new SqlParameterValue(Types.OTHER, embedding.toString());
	}

	public static SqlParameterValue toSqlVector(float[] embedding) {
		return new SqlParameterValue(Types.OTHER, Arrays.toString(embedding));
	}
}
