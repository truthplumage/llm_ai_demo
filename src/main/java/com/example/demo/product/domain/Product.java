package com.example.demo.product.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(
	UUID id,
	UUID sellerId,
	String name,
	String description,
	BigDecimal price,
	int stock,
	String status) {
}
