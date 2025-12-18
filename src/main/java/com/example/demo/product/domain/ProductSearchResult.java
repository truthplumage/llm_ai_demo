package com.example.demo.product.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSearchResult(UUID id, String name, String description, BigDecimal price, int stock, String status, double score) {
}
