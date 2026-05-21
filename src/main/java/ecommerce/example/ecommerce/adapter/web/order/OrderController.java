package ecommerce.example.ecommerce.adapter.web.order;

import ecommerce.example.ecommerce.application.order.PlaceOrderUseCase;
import ecommerce.example.ecommerce.application.customers.ManageCustomerUseCase; // Inject thêm UseCase CRM
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map; 
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderRepository orderRepository;
    private final ManageCustomerUseCase manageCustomerUseCase; // Khai báo tầng xử lý thăng hạng khách hàng

    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestBody(required = false) Map<String, String> payload, 
            Authentication auth) throws Exception {
        
        String buyerId = auth.getName(); 
        
        // Trích xuất mã khách hàng được gán tại quầy POS ngoài Frontend (nếu có)
        String customerId = (payload != null) ? payload.get("customerId") : null;
        
        // Gọi UseCase tạo đơn hàng của hệ thống và truyền thông tin CRM vào luồng xử lý
        placeOrderUseCase.execute(buyerId, customerId); 
        
        Order latestOrder = orderRepository.findByBuyerId(buyerId)
                .stream()
                .sorted((o1, o2) -> o2.getId().compareTo(o1.getId())) 
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng vừa tạo"));

        // ✅ Khóa cấu hình thông tin tài khoản ngân hàng thực tế của Long
        String BANK_ID = "MB";                 
        String ACCOUNT_NO = "0362720865";    
        String ACCOUNT_NAME = "NGUYEN XUAN LONG"; 
        
        double amount = latestOrder.getTotalAmount() != null ? latestOrder.getTotalAmount().doubleValue() : 100000; 
        String description = "DH" + latestOrder.getId().substring(0, 8); 

        String encodedName = java.net.URLEncoder.encode(ACCOUNT_NAME, java.nio.charset.StandardCharsets.UTF_8);
        String encodedDesc = java.net.URLEncoder.encode(description, java.nio.charset.StandardCharsets.UTF_8);

        String realPayQrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact.png?amount=%.0f&addInfo=%s&accountName=%s",
            BANK_ID, ACCOUNT_NO, amount, encodedDesc, encodedName
        );

        return ResponseEntity.ok(Map.of(
            "orderId", latestOrder.getId(),  
            "payUrl", realPayQrUrl, 
            "message", "Đặt hàng thành công! Hãy quét mã QR để thanh toán."
        ));
    }

    @PostMapping("/{orderId}/mock-payment-success")
    public ResponseEntity<String> mockPaymentSuccess(@PathVariable String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại!"));

        order.setStatus("PAID"); 
        orderRepository.save(order);
        
        // 🌟 TÍNH NĂNG CRM TỰ ĐỘNG: Cộng dồn tổng chi tiêu và kích hoạt thăng hạng chiết khấu cho khách
        if (order.getCustomerId() != null && !order.getCustomerId().isBlank()) {
            manageCustomerUseCase.updateCustomer(
                order.getCustomerId(), 
                null, 
                order.getTotalAmount(), // Cộng dồn số tiền thực trả của hóa đơn này vào totalSpent
                null
            );
        }
        
        return ResponseEntity.ok("Mô phỏng thanh toán thành công! Đơn hàng " + orderId + " đã chuyển sang trạng thái PAID và tích lũy doanh số CRM.");
    }

    @GetMapping("/my-purchases")
    public ResponseEntity<List<Order>> getMyPurchases(Authentication auth) {
        String buyerId = auth.getName();
        return ResponseEntity.ok(orderRepository.findByBuyerId(buyerId));
    }

    @GetMapping("/my-sales")
    public ResponseEntity<List<Order>> getMySales(Authentication auth) {
        String sellerId = auth.getName();
        return ResponseEntity.ok(orderRepository.findBySellerId(sellerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}