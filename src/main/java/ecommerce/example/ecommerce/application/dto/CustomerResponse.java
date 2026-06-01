package ecommerce.example.ecommerce.application.dto;

import java.math.BigDecimal;

// DTO đóng gói dữ liệu trả về cho Frontend, bọc sẵn trường discountPercentage tính toán tự động
public record CustomerResponse(
    String id,
    String phone,
    String fullName,
    String customerType,
    BigDecimal totalSpent,
    int discountPercentage,
    String notes,
    String branchId
) {}