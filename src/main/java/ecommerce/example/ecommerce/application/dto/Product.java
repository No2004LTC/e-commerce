package ecommerce.example.ecommerce.application.dto;

import java.math.BigDecimal;

public record Product(
    String id,
    String productCode,
    String name,
    String description,
    String productImageUrl, 
    BigDecimal price,
    Integer stockQuantity,
    Integer soldQuantity,
    String warehouse,
    String supplier
) {}