package ecommerce.example.ecommerce.application.dto;

import java.math.BigDecimal;

/**
 * DTO trả về doanh thu từng tuần trong tháng — dùng cho biểu đồ đường/cột.
 */
public record WeeklyRevenueDto(
    int    yearWeek,        // Mã tuần dạng YEARWEEK, ví dụ: 202521 (năm 2025, tuần 21)
    String weekLabel,       // Nhãn hiển thị, ví dụ: "Tuần 1", "Tuần 2"...
    BigDecimal totalRevenue // Tổng doanh số của tuần đó
) {}
