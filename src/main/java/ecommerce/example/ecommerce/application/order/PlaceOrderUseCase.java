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
        // 1. Tìm User để lấy Email thật
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

        // 2. LẤY GIỎ HÀNG (Dòng này bị thiếu nên gây ra lỗi 'cannot be resolved')
        var cart = cartGateway.findByUserId(userIdOrUsername)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng của " + userIdOrUsername + " đang trống!"));

        // 3. Tạo đơn hàng (Sử dụng cart ở trên để lấy tổng tiền)
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyerId(realEmail) 
                .sellerId("VIETQR_STORE")
                .totalAmount(cart.getTotalPrice()) // Bây giờ cart đã 'resolved'
                .status("PENDING")
                .build();

        // 4. Lưu đơn hàng và dọn dẹp giỏ hàng
        orderRepository.save(order);
        cartGateway.deleteByUserId(userIdOrUsername);

        log.info("[PLACE ORDER] Đã lưu đơn hàng #{} thành công.", order.getId());

        // 5. Trả về link QR
        return vietQRService.createPaymentUrl(order);
    }
}