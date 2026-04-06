package ecommerce.example.ecommerce.application.dto;

import java.math.BigDecimal;

public record Product(
    String id,
    String ownerId, // Thêm
    String productCode,
    String name,
    String description,
    String productImageUrl,
    java.math.BigDecimal price,
    Integer stockQuantity,
    Integer soldQuantity,
    String warehouse,
    String supplier,
    String status // Thêm
) {}