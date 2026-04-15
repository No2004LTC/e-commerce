package ecommerce.example.ecommerce.application.order;

import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.infrastructure.payment.vnpay.VnPayService; // Import VnPay
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaceOrderUseCase {
    private final CartGateway cartGateway;
    private final OrderRepository orderRepository;
    private final VnPayService vnPayService; // Đổi từ MoMo sang VNPAY

    @Transactional
    public String execute(String buyerId) throws Exception {
        // 1. Lấy giỏ hàng
        var cart = cartGateway.findByUserId(buyerId).orElseThrow();
        
        // 2. Tạo đơn hàng (Lưu vào MySQL)
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyerId(buyerId)
                .sellerId("VNPAY_STORE")
                .totalAmount(cart.getTotalPrice())
                .status("PENDING")
                .build();

        orderRepository.save(order);
        
        // 3. Xóa giỏ hàng sau khi đã tạo đơn
        cartGateway.deleteByUserId(buyerId);

        // 4. Gọi VNPAY để lấy link thanh toán
        return vnPayService.createPaymentUrl(order);
    }
}