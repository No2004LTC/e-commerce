package ecommerce.example.ecommerce.application.order;

import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.application.products.ProductService;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.infrastructure.payment.momo.MoMoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaceOrderUseCase {
    private final CartGateway cartGateway;
    private final OrderRepository orderRepository;
    private final MoMoService moMoService;

    @Transactional
    public String execute(String buyerId) throws Exception {
        // 1. Lấy giỏ hàng (Giả sử bạn đã có logic này)
        var cart = cartGateway.findByUserId(buyerId).orElseThrow();
        
        // 2. Tạo Order (Ví dụ đơn giản cho 1 đơn hàng)
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyerId(buyerId)
                .totalAmount(cart.getTotalPrice()) // Tùy vào cách bạn tính tiền
                .status("PENDING")
                .build();

        orderRepository.save(order);
        
        // 3. Xóa giỏ hàng
        cartGateway.deleteByUserId(buyerId);

        // 4. Trả về link MoMo
        return moMoService.createPaymentUrl(order);
    }
}