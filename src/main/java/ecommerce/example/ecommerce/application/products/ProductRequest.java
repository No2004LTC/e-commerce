package ecommerce.example.ecommerce.application.products;

import java.math.BigDecimal;

public record ProductRequest(
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    String warehouse,
    String supplier
) {}