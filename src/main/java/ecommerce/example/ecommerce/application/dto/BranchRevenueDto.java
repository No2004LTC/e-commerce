package ecommerce.example.ecommerce.application.dto;

import java.math.BigDecimal;

/**
 * DTO trả về doanh thu của từng chi nhánh — dùng cho biểu đồ tròn.
 */
public record BranchRevenueDto(
    String branchId,        // ownerId / sellerId của chi nhánh
    String branchName,      // username của chi nhánh (join từ bảng users)
    BigDecimal totalRevenue // Tổng doanh số đơn hàng PAID của chi nhánh đó
) {}
