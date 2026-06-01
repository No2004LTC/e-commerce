package ecommerce.example.ecommerce.adapter.web.order;

import ecommerce.example.ecommerce.application.order.ExportOrdersUseCase;
import ecommerce.example.ecommerce.application.order.PlaceOrderUseCase;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import java.util.ArrayList;
import java.util.Optional;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * REST Controller cho luồng Đặt hàng & Quản lý Order.
 *
 * POST /api/orders            — Đặt hàng từ giỏ Redis, tích hợp CRM chiết khấu
 * GET  /api/orders/my-purchases — Lịch sử mua hàng của người dùng
 * GET  /api/orders/my-sales   — Lịch sử bán hàng của shop
 * GET  /api/orders/{id}       — Chi tiết một đơn hàng cụ thể
 * POST /api/orders/{id}/mock-payment-success — Mô phỏng thanh toán thành công
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderRepository orderRepository;
    private final ExportOrdersUseCase exportOrdersUseCase;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestParam(name = "branchId", required = false) String branchId,
            Authentication auth) {
        log.info("[GET ORDERS] Query orders with branchId: {}", branchId);
        
        User user = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByUsername(auth.getName()))
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại: " + auth.getName()));

        boolean isAdmin = user.getRole() != null &&
                "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getName());
        boolean isBranchOrStaff = user.getRole() != null &&
                ("ROLE_BRANCH".equalsIgnoreCase(user.getRole().getName()) ||
                 "ROLE_STAFF".equalsIgnoreCase(user.getRole().getName()));

        List<Order> orders;

        if (isBranchOrStaff) {
            // Branch/Staff: always scoped to their own branch subtree
            String targetId = user.getId().toString();
            log.info("[GET ORDERS] Branch/Staff user {} → scoping to branchId={}", user.getUsername(), targetId);
            orders = orderRepository.findByBranchOrParentChain(targetId);
        } else if (isAdmin && (branchId == null || branchId.isBlank())) {
            // Admin with no filter → return ALL orders across every branch
            log.info("[GET ORDERS] Admin user {} → returning ALL orders", user.getUsername());
            orders = orderRepository.findAll();
        } else {
            // Admin with explicit branchId filter → scope to that branch subtree
            String targetId = (branchId != null && !branchId.isBlank()) ? branchId : user.getId().toString();
            log.info("[GET ORDERS] Filtered query for branchId={}", targetId);
            orders = orderRepository.findByBranchOrParentChain(targetId);
        }

        // Enrich seller name from DB for display
        for (Order order : orders) {
            if (order.getSellerId() != null && !order.getSellerId().isBlank()) {
                try {
                    java.util.UUID.fromString(order.getSellerId());
                    userRepository.findById(new ecommerce.example.ecommerce.domain.user.UserId(order.getSellerId()))
                            .ifPresent(u -> {
                                String name = u.getFullName() != null && !u.getFullName().isBlank()
                                        ? u.getFullName()
                                        : u.getUsername();
                                order.setSellerName(name);
                            });
                } catch (IllegalArgumentException e) {
                    order.setSellerName(order.getSellerId());
                }
            }
        }
        return ResponseEntity.ok(orders);
    }


    // =========================================================================
    // POST /api/orders — Đặt hàng
    // Body (tuỳ chọn): { "customerPhone": "0901234567" }
    // customerPhone: SĐT khách hàng CRM tại quầy POS để áp dụng chiết khấu
    // =========================================================================
    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestBody(required = false) Map<String, String> payload,
            Authentication auth) throws Exception {

        String buyerId = auth.getName(); // email hoặc username từ JWT

        // Trích xuất SĐT khách hàng CRM từ payload POS (nếu có)
        String orderId = (payload != null) ? payload.get("id") : null;
        String customerPhone = (payload != null) ? payload.get("customerPhone") : null;
        String paymentMethod = (payload != null) ? payload.get("paymentMethod") : "CHUYỂN KHOẢN";
        String sellerId = (payload != null) ? payload.get("sellerId") : null;

        // Thực thi luồng đặt hàng hoàn chỉnh và lấy Order trực tiếp
        Order latestOrder = placeOrderUseCase.execute(orderId, buyerId, customerPhone, paymentMethod, sellerId);

        double amount = latestOrder.getTotalAmount() != null
                ? latestOrder.getTotalAmount().doubleValue()
                : 100_000;

        String payQrUrl = "";
        if ("CHUYỂN KHOẢN".equalsIgnoreCase(latestOrder.getPaymentMethod())) {
            String bankId = "970422"; // MB Bank BIN
            String accountNo = "0362720865";
            String accountName = "NGUYEN XUAN LONG";
            String encodedName = URLEncoder.encode(accountName, java.nio.charset.StandardCharsets.UTF_8);
            // Hiển thị ảnh QR chứa mã đơn hàng định dạng UUID (latestOrder.getId())
            payQrUrl = String.format(
                "https://api.vietqr.io/image/%s-%s-compact.jpg?amount=%.0f&addInfo=%s&accountName=%s",
                bankId, accountNo, amount, latestOrder.getId(), encodedName
            );
        }

        return ResponseEntity.ok(Map.of(
            "orderId",  latestOrder.getId(),
            "payUrl",   payQrUrl,
            "amount",   amount,
            "message",  "Đặt hàng thành công! Hãy quét mã QR để thanh toán."
        ));
    }

    // =========================================================================
    // POST /api/orders/{orderId}/mock-payment-success
    // Mô phỏng webhook thanh toán thành công (dành cho môi trường DEV/TEST)
    // =========================================================================
    @PostMapping("/{orderId}/mock-payment-success")
    public ResponseEntity<String> mockPaymentSuccess(@PathVariable String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + orderId));

        order.setStatus("PAID");
        orderRepository.save(order);

        log.info("[MOCK PAYMENT] Đơn hàng {} đã chuyển sang PAID", orderId);

        return ResponseEntity.ok(
            "Mô phỏng thanh toán thành công! Đơn hàng " + orderId + " đã chuyển sang PAID."
        );
    }

    // =========================================================================
    // GET /api/orders/my-purchases — Lịch sử mua hàng
    // =========================================================================
    @GetMapping("/my-purchases")
    public ResponseEntity<List<Order>> getMyPurchases(Authentication auth) {
        String buyerId = auth.getName();
        return ResponseEntity.ok(orderRepository.findByBuyerId(buyerId));
    }

    // =========================================================================
    // GET /api/orders/my-sales — Lịch sử bán hàng
    // =========================================================================
    @GetMapping("/my-sales")
    public ResponseEntity<List<Order>> getMySales(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByUsername(auth.getName()))
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại: " + auth.getName()));
        String sellerName = user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName()
                : user.getUsername();
        return ResponseEntity.ok(orderRepository.findBySellerId(sellerName));
    }

    // =========================================================================
    // GET /api/orders/{id} — Chi tiết đơn hàng
    // =========================================================================
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateStatusRequest request) {
        log.info("[UPDATE STATUS] Order: {} | New Status: {}", id, request.status());
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + id));
        order.setStatus(request.status());
        orderRepository.save(order);
        return ResponseEntity.ok(order);
    }

    // =========================================================================
    // GET /api/orders/export — Xuất danh sách đơn hàng ra file Excel
    //
    // Query param: ?scope=all  → Xuất tất cả đơn hàng (admin)
    //              (không có)  → Chỉ xuất đơn của user đang đăng nhập
    //
    // Headers phản hồi:
    //   Content-Disposition: attachment; filename=hoadon.xlsx
    //   Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
    // =========================================================================
    @GetMapping("/export")
    public void exportOrders(
            @RequestParam(name = "scope", required = false) String scope,
            @RequestParam(name = "ids", required = false) String ids,
            Authentication auth,
            HttpServletResponse response) throws Exception {

        // Cấu hình header phản hồi — BẮT BUỘC trước khi ghi stream
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=hoadon.xlsx"
        );
        // Cho phép Frontend đọc header Content-Disposition qua CORS
        response.setHeader("Access-Control-Expose-Headers", HttpHeaders.CONTENT_DISPOSITION);

        // Ghi Workbook trực tiếp ra OutputStream của response — hỗ trợ cả lọc ids
        exportOrdersUseCase.execute(auth.getName(), scope, ids, response.getOutputStream());

        log.info("[EXPORT] User {} đã xuất Excel đơn hàng (scope={}, ids={}).", auth.getName(), scope, ids);
    }
}