package ecommerce.example.ecommerce.application.dto;

public record Supplier(
    String code,
    String name,
    String address,
    String phoneNumber,
    String email,
    String providedProduct,
    Integer quantity,
    java.math.BigDecimal unitPrice
) {}
