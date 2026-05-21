package ecommerce.example.ecommerce.application.order;

import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.domain.user.UserId; 
import ecommerce.example.ecommerce.domain.customers.Customer; // Import thực thể Customer
import ecommerce.example.ecommerce.domain.customers.CustomerRepository; // Import Repository Customer
import ecommerce.example.ecommerce.infrastructure.payment.vietqr.VietQRService;
import ecommerce.example.ecommerce.adapter.persistence.User.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceOrderUseCase {
    private final CartGateway cartGateway;
    private final OrderRepository orderRepository;
    private final VietQRService vietQRService;
    private final UserJpaRepository userJpaRepository;
    private final CustomerRepository customerRepository; // Inject kho lưu trữ dữ liệu khách hàng

    @Transactional
    public String execute(String userIdOrUsername, String customerId) throws Exception {
        
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

        // 🌟 XỬ LÝ CRM: Tính toán chiết khấu tự động dựa trên phân hạng khách hàng
        BigDecimal originalTotal = cart.getTotalPrice();
        BigDecimal finalTotal = originalTotal;

        if (customerId != null && !customerId.isBlank()) {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer != null) {
                int discountPercent = customer.getDiscountPercentage(); // Lấy % giảm giá tự động (5% - 25%)
                log.info("[CRM INTEGRATION] Áp dụng giảm giá bậc hạng cho khách hàng: {}%", discountPercent);
                
                // Công thức tính tiền sau chiết khấu: finalTotal = originalTotal * (100 - discountPercent) / 100
                BigDecimal discountFactor = BigDecimal.valueOf((100 - discountPercent) / 100.0);
                finalTotal = originalTotal.multiply(discountFactor);
            }
        }

        // Tạo đơn hàng lưu xuống MySQL database
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .buyerId(realEmail) 
                .sellerId("VIETQR_STORE")
                .customerId(customerId) // Lưu vết khóa ngoại khách hàng sở hữu hóa đơn này
                .totalAmount(finalTotal) // Ghi nhận số tiền cuối cùng sau khi đã giảm giá
                .status("PENDING")
                .build();

        orderRepository.save(order);
        cartGateway.deleteByUserId(userIdOrUsername);

        log.info("[PLACE ORDER] Đã lưu đơn hàng #{} thành công với số tiền: {} VNĐ", order.getId(), finalTotal);

        return vietQRService.createPaymentUrl(order);
    }
}