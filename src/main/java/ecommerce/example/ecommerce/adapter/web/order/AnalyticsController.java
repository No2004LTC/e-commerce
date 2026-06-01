package ecommerce.example.ecommerce.adapter.web.order;

import ecommerce.example.ecommerce.adapter.persistence.products.ProductJpaRepository;
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
import java.util.Map;

/**
 * REST Controller cung cấp dữ liệu Dashboard / Analytics.
 *
 * Tất cả API cần xác thực JWT. userId của cửa hàng lớn được bóc tách từ token.
 *
 * GET /api/analytics/branches-revenue — Doanh thu từng chi nhánh (biểu đồ tròn)
 * GET /api/analytics/weekly-revenue   — Doanh thu theo tuần trong tháng (biểu đồ đường)
 * GET /api/analytics/low-stock        — Số sản phẩm sắp hết hàng (tồn kho < 5)
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final OrderRepository      orderRepository;
    private final UserRepository       userRepository;
    private final ProductJpaRepository productJpaRepository;  // Dùng để đếm sản phẩm hết hàng

    // =========================================================================
    // GET /api/analytics/branches-revenue
    // Trả về tổng doanh số của từng chi nhánh để Frontend vẽ biểu đồ tròn.
    // Chỉ tính đơn hàng có status = 'PAID'.
    // =========================================================================
    @GetMapping("/branches-revenue")
    public ResponseEntity<List<BranchRevenueDto>> getBranchesRevenue(
            @RequestParam(name = "shopOwnerId", required = false) String shopOwnerId,
            Authentication auth) {
        String parentId = resolveOwnerId(auth);
        if (shopOwnerId != null && !shopOwnerId.isBlank() && isAdmin(auth)) {
            parentId = shopOwnerId;
        }

        List<Object[]> rawData = orderRepository.findBranchesRevenue(parentId);

        // SQL trả về: row[0] = branch_label (COALESCE branch_name, seller_name, seller_id)
        //             row[1] = branch_revenue (SUM total_amount)
        // Không cần lookup thêm DB — branch_label đã là tên thân thiện từ lúc POS lưu đơn
        List<BranchRevenueDto> result = new ArrayList<>();
        for (Object[] row : rawData) {
            String branchLabel = row[0] != null ? row[0].toString() : "Chi nhánh";
            BigDecimal revenue = row[1] != null
                ? new BigDecimal(row[1].toString())
                : BigDecimal.ZERO;

            result.add(new BranchRevenueDto(branchLabel, revenue));
        }

        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // GET /api/analytics/weekly-revenue
    // Trả về doanh thu của toàn chuỗi phân tách theo từng tuần trong tháng hiện tại.
    // Dùng YEARWEEK MySQL function (mode=1: tuần bắt đầu từ thứ Hai).
    // =========================================================================
    @GetMapping("/weekly-revenue")
    public ResponseEntity<List<WeeklyRevenueDto>> getWeeklyRevenue(
            @RequestParam(name = "shopOwnerId", required = false) String shopOwnerId,
            Authentication auth) {
        String parentId = resolveOwnerId(auth);
        if (shopOwnerId != null && !shopOwnerId.isBlank() && isAdmin(auth)) {
            parentId = shopOwnerId;
        }

        List<Object[]> rawData = orderRepository.findWeeklyRevenue(parentId);

        List<WeeklyRevenueDto> result = new ArrayList<>();

        for (Object[] row : rawData) {
            // row[0] = "Tuần XX - YYYY"  (trực tiếp từ CONCAT trong SQL)
            // row[1] = SUM(total_amount)
            String weekLabel = row[0] != null ? row[0].toString() : "";
            BigDecimal revenue = row[1] != null
                ? new BigDecimal(row[1].toString())
                : BigDecimal.ZERO;

            result.add(new WeeklyRevenueDto(0, weekLabel, revenue));
        }

        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // GET /api/analytics/low-stock
    // Trả về số lượng sản phẩm có tồn kho < 5 trong toàn bộ chuỗi của user.
    // Quét cả chi nhánh gốc và mọi chi nhánh con (parent_id).
    // =========================================================================
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Long>> getLowStockCount(
            @RequestParam(name = "shopOwnerId", required = false) String shopOwnerId,
            Authentication auth) {
        String ownerId = resolveOwnerId(auth);
        if (shopOwnerId != null && !shopOwnerId.isBlank() && isAdmin(auth)) {
            ownerId = shopOwnerId;
        }
        long count = productJpaRepository.countLowStockByChain(ownerId);
        return ResponseEntity.ok(Map.of("lowStockCount", count));
    }

    private boolean isAdmin(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .or(() -> userRepository.findByUsername(auth.getName()))
            .orElse(null);
        return user != null && user.getRole() != null && "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getName());
    }

    // ── Helper: lấy UUID của User đang đăng nhập từ JWT token ─────────────────
    private String resolveUserId(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .or(() -> userRepository.findByUsername(auth.getName()))
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        return user.getId().toString();
    }

    // ── Helper: lấy UUID của User cha (Owner) nếu User đang đăng nhập là chi nhánh con ─
    private String resolveOwnerId(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
            .or(() -> userRepository.findByUsername(auth.getName()))
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
        return (user.getParentId() != null && !user.getParentId().isBlank())
            ? user.getParentId()
            : user.getId().toString();
    }
}
