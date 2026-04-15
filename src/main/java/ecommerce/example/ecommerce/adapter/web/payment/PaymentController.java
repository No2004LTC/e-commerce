package ecommerce.example.ecommerce.adapter.web.payment;

import ecommerce.example.ecommerce.application.products.ProductService;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.domain.products.ProductId;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment") // Đường dẫn chung
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    // --- 1. ĐƯỜNG DÂY NÓNG CỦA VNPAY ---
    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> handleVnPayIPN(HttpServletRequest request) {
        String orderId = request.getParameter("vnp_TxnRef");
        String responseCode = request.getParameter("vnp_ResponseCode");

        log.info("[VNPAY] Callback nhận được cho đơn hàng: {}, Mã lỗi: {}", orderId, responseCode);

        if ("00".equals(responseCode)) {
            updateOrderToPaid(orderId);
            return ResponseEntity.ok("Success");
        }
        return ResponseEntity.badRequest().body("Payment Failed");
    }

    // --- 2. ĐƯỜNG DÂY NÓNG CỦA MOMO (Cái bạn đang có) ---
    @PostMapping("/momo/callback")
    public ResponseEntity<Void> handleMoMoCallback(@RequestBody Map<String, Object> body) {
        String orderId = (String) body.get("orderId");
        String resultCode = String.valueOf(body.get("resultCode"));

        log.info("[MOMO] Callback nhận được cho đơn hàng: {}, Mã lỗi: {}", orderId, resultCode);

        if ("0".equals(resultCode)) {
            updateOrderToPaid(orderId);
        }
        return ResponseEntity.noContent().build();
    }

    // Hàm dùng chung để cập nhật Database và trừ kho
    private void updateOrderToPaid(String orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        if (!"PAID".equals(order.getStatus())) { // Tránh cập nhật 2 lần
            order.setStatus("PAID");
            orderRepository.save(order);
            
            // TODO: Gọi productService.updateInventory(...) ở đây
            log.info("--- [HỆ THỐNG] Đơn hàng {} đã thanh toán thành công. Đã trừ kho! ---", orderId);
            
            // Đây chính là chỗ để bạn gọi hàm gửi Email: 
            // emailService.sendSuccessEmail(order.getBuyerEmail());
        }
    }
}