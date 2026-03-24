package ecommerce.example.ecommerce.application.products;

import java.math.BigDecimal;
// Nhận input 
public record ProductRequest(
    String name,
    String description,
    BigDecimal price,
    Integer stockQuantity,
    String warehouse,
    String supplier
) {}