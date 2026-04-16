package ecommerce.example.ecommerce.adapter.web.order;

import ecommerce.example.ecommerce.application.order.CreateOrderUseCase;
import ecommerce.example.ecommerce.application.order.PlaceOrderUseCase;
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

    /**
     * 1. Đặt hàng (Từ giỏ hàng Redis chuyển thành Order MySQL)
     */
   @PostMapping
  
public ResponseEntity<?> placeOrder(Authentication auth) throws Exception {
    String buyerId = auth.getName(); 
    
    // 1. Thực hiện đặt hàng và lấy link thanh toán
    String payUrl = placeOrderUseCase.execute(buyerId); 
    
    // 2. Vì PlaceOrderUseCase vừa mới save Order vào DB, 
    // bạn có thể lấy đơn hàng mới nhất của Buyer này ra để lấy ID
    Order latestOrder = orderRepository.findByBuyerId(buyerId)
            .stream()
            .sorted((o1, o2) -> o2.getId().compareTo(o1.getId())) // Sắp xếp lấy cái mới nhất (tạm thời)
            .findFirst()
            .orElseThrow();

    // 3. Trả về JSON cho Postman dễ nhìn
    return ResponseEntity.ok(Map.of(
        "orderId", latestOrder.getId(),  // Trả về cái ID dài bf44f1db...
        "payUrl", payUrl,                // Trả về link QR
        "message", "Đặt hàng thành công! Hãy quét mã QR để thanh toán."
    ));
}

    /**
     * 2. Xem lịch sử mua hàng (Cho người mua)
     */
    @GetMapping("/my-purchases")
    public ResponseEntity<List<Order>> getMyPurchases(Authentication auth) {
        String buyerId = auth.getName();
        return ResponseEntity.ok(orderRepository.findByBuyerId(buyerId));
    }

    /**
     * 3. Xem các đơn hàng khách đã mua (Cho người bán/chủ kho)
     */
    @GetMapping("/my-sales")
    public ResponseEntity<List<Order>> getMySales(Authentication auth) {
        String sellerId = auth.getName();
        return ResponseEntity.ok(orderRepository.findBySellerId(sellerId));
    }

    /**
     * 4. Xem chi tiết 1 đơn hàng
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable String id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}