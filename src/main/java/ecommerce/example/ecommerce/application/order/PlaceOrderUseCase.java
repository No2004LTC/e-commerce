package ecommerce.example.ecommerce.application.order;

import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.domain.user.UserId; 
import ecommerce.example.ecommerce.infrastructure.payment.vietqr.VietQRService;
import ecommerce.example.ecommerce.adapter.persistence.User.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceOrderUseCase {
    private final CartGateway cartGateway;
    private final OrderRepository orderRepository;
    private final VietQRService vietQRService;
    private final UserJpaRepository userJpaRepository;

    @Transactional
    public String execute(String userIdOrUsername) throws Exception {
        
        var user = userJpaRepository.findByUsername(userIdOrUsername)
                .stream().findFirst()
                .or(() -> {
                    try {
                        return userJpaRepository.findById(UserId.fromString(userIdOrUsername));
                    } catch (Exception e) {
                        return java.util.Optional.empty();
                    }
                })
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + userIdOrUsername));

        String realEmail = user.getEmail();
        log.info("[PLACE ORDER] Đã tìm thấy Email thật: {}", realEmail);

        
        var cart = cartGateway.findByUserId(userIdOrUsername)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng của " + userIdOrUsername + " đang trống!"));

        
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyerId(realEmail) 
                .sellerId("VIETQR_STORE")
                .totalAmount(cart.getTotalPrice()) 
                .status("PENDING")
                .build();

        
        orderRepository.save(order);
        cartGateway.deleteByUserId(userIdOrUsername);

        log.info("[PLACE ORDER] Đã lưu đơn hàng #{} thành công.", order.getId());

       
        return vietQRService.createPaymentUrl(order);
    }
}