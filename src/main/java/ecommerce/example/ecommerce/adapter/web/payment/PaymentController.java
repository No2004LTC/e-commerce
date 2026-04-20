package ecommerce.example.ecommerce.adapter.web.payment;

import ecommerce.example.ecommerce.infrastructure.email.EmailService;
import ecommerce.example.ecommerce.application.products.ProductService;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.infrastructure.payment.vietqr.VietQRService; // Import thằng mới này vào
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final EmailService emailService;
    private final VietQRService vietQRService; // <--- CẦN INJECT THẰNG NÀY VÀO

    /**
     * 1. API TẠO MÃ QR (Đây là chỗ bạn cần gọi để lấy mã QR)
     * Bạn gọi: GET http://localhost:8080/api/payment/create-qr/362085077d233
     */
    @GetMapping("/create-qr/{orderId}")
    public ResponseEntity<?> createVietQR(@PathVariable String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại: " + orderId));

        // Gọi service để lấy link ảnh .png
        String qrUrl = vietQRService.createPaymentUrl(order);

        log.info("[VIETQR] Đã tạo link ảnh QR thành công cho đơn hàng: {}", orderId);
        
        // Trả về link ảnh cho Frontend
        return ResponseEntity.ok(Map.of("qrUrl", qrUrl));
    }

    /**
     * 2. API XÁC NHẬN THANH TOÁN (Dành cho Admin hoặc Demo)
     * Vì VietQR không tự gọi callback, sau khi quét xong bạn gọi API này để hệ thống nổ Email
     */
    @PostMapping("/confirm-payment/{orderId}")
    public ResponseEntity<?> manualConfirm(@PathVariable String orderId) {
        log.info("[MANUAL CONFIRM] Xác nhận thanh toán tay cho đơn hàng: {}", orderId);
        updateOrderToPaid(orderId);
        return ResponseEntity.ok(Map.of("message", "Đã xác nhận thanh toán và gửi Email thành công!"));
    }

    // --- HÀM CẬP NHẬT DB VÀ GỬI EMAIL (GIỮ NGUYÊN) ---
    private void updateOrderToPaid(String orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));

        if (!"PAID".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderRepository.save(order);
            
            // Gửi email
            emailService.sendPaymentSuccessEmail(
                order.getBuyerId(), 
                order.getId(), 
                order.getTotalAmount().longValue()
            );

            log.info("--- [THÀNH CÔNG] Đơn hàng {} đã PAID. Đã gửi email xác nhận. ---", orderId);
        }
    }
}