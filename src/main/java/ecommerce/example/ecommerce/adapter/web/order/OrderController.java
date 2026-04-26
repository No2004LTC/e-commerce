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

    
   @PostMapping
  
public ResponseEntity<?> placeOrder(Authentication auth) throws Exception {
    String buyerId = auth.getName(); 
    
    
    String payUrl = placeOrderUseCase.execute(buyerId); 
    
    Order latestOrder = orderRepository.findByBuyerId(buyerId)
            .stream()
            .sorted((o1, o2) -> o2.getId().compareTo(o1.getId())) 
            .findFirst()
            .orElseThrow();

    return ResponseEntity.ok(Map.of(
        "orderId", latestOrder.getId(),  
        "payUrl", payUrl,                
        "message", "Đặt hàng thành công! Hãy quét mã QR để thanh toán."
    ));
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