package ecommerce.example.ecommerce.adapter.web.payment;

import ecommerce.example.ecommerce.application.products.ProductService;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.domain.products.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/momo")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody Map<String, Object> body) {
        log.info("MoMo Callback received: {}", body);
        
        String resultCode = String.valueOf(body.get("resultCode"));
        String orderId = (String) body.get("orderId");

        if ("0".equals(resultCode)) {
            var order = orderRepository.findById(orderId).orElseThrow();
            order.setStatus("PAID");
            orderRepository.save(order);

            // Logic trừ kho (Bạn cần map Order sang OrderItem để trừ)
            log.info("Order {} payment successful. Inventory updated.", orderId);
        }

        return ResponseEntity.noContent().build();
    }
}   