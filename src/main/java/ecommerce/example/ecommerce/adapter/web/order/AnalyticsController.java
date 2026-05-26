package ecommerce.example.ecommerce.adapter.web.order;

import ecommerce.example.ecommerce.application.dto.BranchRevenueDto;
import ecommerce.example.ecommerce.application.dto.WeeklyRevenueDto;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import ecommerce.example.ecommerce.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller cung cấp dữ liệu Dashboard / Analytics.
 *
 * Tất cả API cần xác thực JWT. userId của cửa hàng lớn được bóc tách từ token.
 *
 * GET /api/analytics/branches-revenue — Doanh thu từng chi nhánh (biểu đồ tròn)
 * GET /api/analytics/weekly-revenue   — Doanh thu theo tuần trong tháng (biểu đồ đường)
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final OrderRepository orderRepository;
    private final UserRepository  userRepository;

    // =========================================================================
    // GET /api/analytics/branches-revenue
    // Trả về tổng doanh số của từng chi nhánh để Frontend vẽ biểu đồ tròn.
    // Chỉ tính đơn hàng có status = 'PAID'.
    // =========================================================================
    @GetMapping("/branches-revenue")
    public ResponseEntity<List<BranchRevenueDto>> getBranchesRevenue(Authentication auth) {
        String parentId = resolveUserId(auth);

        List<Object[]> rawData = orderRepository.findBranchesRevenue(parentId);

        // Map dữ liệu thô [sellerId, totalAmount] → DTO kèm tên chi nhánh
        List<BranchRevenueDto> result = new ArrayList<>();
        for (Object[] row : rawData) {
            String sellerId = (String) row[0];
            BigDecimal revenue = row[1] != null
                ? new BigDecimal(row[1].toString())
                : BigDecimal.ZERO;

            String branchName = sellerId;
            try {
                if (sellerId != null && sellerId.length() == 36) {
                    java.util.UUID.fromString(sellerId);
                    branchName = userRepository.findById(new UserId(sellerId))
                        .map(User::getUsername)
                        .orElse(sellerId);
                } else {
                    branchName = userRepository.findByUsername(sellerId)
                        .or(() -> userRepository.findByEmail(sellerId))
                        .map(User::getUsername)
                        .orElse(sellerId);
                }
            } catch (Exception e) {
                branchName = userRepository.findByUsername(sellerId)
                    .or(() -> userRepository.findByEmail(sellerId))
                    .map(User::getUsername)
                    .orElse(sellerId);
            }

            result.add(new BranchRevenueDto(sellerId, branchName, revenue));
        }

        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // GET /api/analytics/weekly-revenue
    // Trả về doanh thu của toàn chuỗi phân tách theo từng tuần trong tháng hiện tại.
    // Dùng YEARWEEK MySQL function (mode=1: tuần bắt đầu từ thứ Hai).
    // =========================================================================
    @GetMapping("/weekly-revenue")
    public ResponseEntity<List<WeeklyRevenueDto>> getWeeklyRevenue(Authentication auth) {
        String parentId = resolveUserId(auth);

        List<Object[]> rawData = orderRepository.findWeeklyRevenue(parentId);

        List<WeeklyRevenueDto> result = new ArrayList<>();
        int weekIndex = 1; // Nhãn tuần nội bộ trong tháng: Tuần 1, Tuần 2...

        for (Object[] row : rawData) {
            String rawDate = row[0] != null ? row[0].toString() : "";
            String formattedDate = rawDate;
            if (rawDate.length() == 10 && rawDate.charAt(4) == '-' && rawDate.charAt(7) == '-') {
                String year = rawDate.substring(0, 4);
                String month = rawDate.substring(5, 7);
                String day = rawDate.substring(8, 10);
                formattedDate = day + "-" + month + "-" + year;
            }
            BigDecimal revenue = row[1] != null
                ? new BigDecimal(row[1].toString())
                : BigDecimal.ZERO;

            result.add(new WeeklyRevenueDto(0, formattedDate, revenue));
        }

        return ResponseEntity.ok(result);
    }

    // ── Helper: lấy UUID của User đang đăng nhập từ JWT token ─────────────────
    private String resolveUserId(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .or(() -> userRepository.findByUsername(auth.getName()))
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        return user.getId().toString();
    }
}
