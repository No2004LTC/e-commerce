package ecommerce.example.ecommerce.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * DTO trả về doanh thu của từng chi nhánh — dùng cho biểu đồ tròn.
 */
public record BranchRevenueDto(
    @JsonProperty("branch_label")
    String branchLabel,
    @JsonProperty("branch_revenue")
    BigDecimal branchRevenue
) {}
