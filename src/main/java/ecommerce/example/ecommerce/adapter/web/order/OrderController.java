package ecommerce.example.ecommerce.adapter.web.order;

import ecommerce.example.ecommerce.application.order.CreateOrderUseCase;
import ecommerce.example.ecommerce.application.order.PlaceOrderUseCase;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> placeOrder(Authentication auth) throws Exception {
        // auth.getName() trả về userId từ JWT
        String buyerId = auth.getName(); 
        
        // UseCase này bây giờ trả về String (payUrl)
        String payUrl = placeOrderUseCase.execute(buyerId); 
        
        // Trả về String cho Postman
        return ResponseEntity.ok(payUrl);
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