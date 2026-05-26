package ecommerce.example.ecommerce.application.order;

import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.domain.Cart.Cart;
import ecommerce.example.ecommerce.domain.Cart.CartItem;
import ecommerce.example.ecommerce.domain.customers.Customer;
import ecommerce.example.ecommerce.domain.customers.CustomerRepository;
import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderItem;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import ecommerce.example.ecommerce.infrastructure.payment.vietqr.VietQRService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Random;

/**
 * UseCase: Đặt hàng từ giỏ hàng Redis — luồng hoàn chỉnh Production-Ready.
 *
 * Luồng xử lý đầy đủ:
 * 1. Tra cứu User qua UserRepository (domain interface — Clean Architecture)
 * 2. Lấy Cart từ Redis qua CartGateway
 * 3. Tra cứu CRM khách hàng theo SĐT -> Nếu trống hoặc không tồn tại, tự động tạo khách hàng vãng lai ngẫu nhiên tránh lỗi 500
 * 4. Chuyển CartItems -> OrderItems (đóng băng giá tại thời điểm mua, tính toán nhân số lượng và trừ tồn kho vật lý)
 * 5. Tạo Order PAID và lưu xuống MySQL (cascade lưu cả OrderItems)
 * 6. Xóa Cart trong Redis sau khi đặt hàng thành công
 * 7. Cộng dồn totalSpent vào Customer để CRM tự động nâng hạng
 * 8. Trả về URL thanh toán VietQR
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceOrderUseCase {

    private static final String[] HO = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Phan", "Đỗ", "Bùi"};
    private static final String[] DEM_TEN = {"Hoàng Anh", "Việt Anh", "Tuấn Anh", "Minh Khôi", "Gia Bảo", "Quốc Anh", "Đức Minh"};

    private final CartGateway cartGateway;
    private final OrderRepository orderRepository;
    private final VietQRService vietQRService;
    private final UserRepository userRepository;           // ✅ Domain interface
    private final CustomerRepository customerRepository;   // ✅ Domain interface
    private final ProductRepository productRepository;     // ✅ Domain interface

    @Transactional
    public String execute(String usernameOrId, String customerPhone, String paymentMethod) throws Exception {
        try {
            // ── Bước 1: Tra cứu danh tính người dùng ──────────────────────────────
            var user = userRepository.findByEmail(usernameOrId)
                    .or(() -> userRepository.findByUsername(usernameOrId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + usernameOrId));

            String realEmail = user.getEmail();
            log.info("[PLACE ORDER] User: {} | Email: {}", usernameOrId, realEmail);

            // ── Bước 2: Lấy giỏ hàng từ Redis ────────────────────────────────────
            Cart cart = cartGateway.findByUserId(usernameOrId)
                    .orElseThrow(() -> new RuntimeException("Giỏ hàng của " + usernameOrId + " đang trống!"));

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new RuntimeException("Giỏ hàng không có sản phẩm nào để đặt hàng!");
            }

            // ── Bước 3: Tra cứu CRM theo SĐT & xử lý tạo tự động khách hàng vãng lai ──
            Customer matchedCustomer = null;

            if (customerPhone == null || customerPhone.isBlank()) {
                // Tự động tạo ngẫu nhiên khách hàng vãng lai mới
                matchedCustomer = generateRandomWalkInCustomer();
            } else {
                matchedCustomer = customerRepository.findByPhone(customerPhone).orElse(null);
                if (matchedCustomer == null) {
                    // Nếu không có trong DB nhưng nhập SĐT, tạo mới khách hàng với SĐT này
                    matchedCustomer = createNewCustomerWithPhone(customerPhone);
                }
            }

            BigDecimal originalTotal = cart.getTotalPrice();
            BigDecimal finalTotal = originalTotal;

            if (matchedCustomer != null) {
                int discountPercent = matchedCustomer.getDiscountPercentage();
                log.info("[CRM] Khách hàng {} | Hạng: {} | Chiết khấu: {}%",
                        matchedCustomer.getFullName(), matchedCustomer.getCustomerType(), discountPercent);

                if (discountPercent > 0) {
                    BigDecimal discountFactor = BigDecimal.valueOf((100.0 - discountPercent) / 100.0);
                    finalTotal = originalTotal.multiply(discountFactor)
                            .setScale(0, java.math.RoundingMode.HALF_UP); // Làm tròn VNĐ
                }
            }

            // ── Bước 4: Chuyển CartItems → OrderItems (đóng băng giá & trừ tồn kho) ──
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : cart.getItems()) {
                // Tra cứu thông tin sản phẩm từ db để kiểm tra và trừ tồn kho vật lý
                var product = productRepository.findById(ProductId.fromString(item.getProductId()))
                        .orElseThrow(() -> new UseCaseException("Không tìm thấy sản phẩm với ID: " + item.getProductId()));

                // Báo lỗi nếu số lượng yêu cầu vượt quá số lượng tồn kho thực tế
                int currentStock = product.getStock() != null ? product.getStock() : 0;
                if (currentStock < item.getQuantity()) {
                    throw new UseCaseException("Sản phẩm " + product.getName() + " đã hết hàng hoặc không đủ tồn kho vật lý");
                }
                
                // Trừ tồn kho vật lý và save lại
                product.setStock(currentStock - item.getQuantity());
                product.setSoldQuantity((product.getSoldQuantity() == null ? 0 : product.getSoldQuantity()) + item.getQuantity());
                if (product.getStock() == 0) {
                    product.setStatus("OUT_OF_STOCK");
                }
                productRepository.save(product);

                BigDecimal unitPrice = item.getPrice();
                if (unitPrice == null) {
                    unitPrice = product.getPrice();
                }
                if (unitPrice == null) {
                    unitPrice = BigDecimal.ZERO;
                }
                
                // Tính toán nhân tổng số lượng (quantity) để gán vào price_at_purchase
                BigDecimal priceAtPurchase = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

                OrderItem orderItem = OrderItem.builder()
                        .productId(item.getProductId())
                        .productName(item.getName())
                        .priceAtPurchase(priceAtPurchase)
                        .quantity(item.getQuantity())
                        .build();
                orderItems.add(orderItem);
            }

            // ── Bước 5: Tạo và lưu Order vào MySQL ────────────────────────────────
            String customerId = (matchedCustomer != null) ? matchedCustomer.getId() : null;
            String sellerName = user.getFullName() != null && !user.getFullName().isBlank()
                    ? user.getFullName()
                    : user.getUsername();

            Order order = Order.builder()
                    .id(UUID.randomUUID().toString())
                    .buyerId(realEmail)
                    .sellerId(sellerName) // Tuyệt đối không lưu chuỗi UUID thô
                    .sellerName(sellerName) // Thiết lập tên Người bán
                    .customerId(customerId)
                    .totalAmount(finalTotal)
                    .status("PAID") // Đặt trực tiếp trạng thái PAID để POS in hóa đơn luôn
                    .paymentMethod(paymentMethod != null && !paymentMethod.isBlank() ? paymentMethod : "CHUYỂN KHOẢN") // Dùng paymentMethod truyền lên
                    .paymentStatus("SUCCESS")      // Thanh toán thành công
                    .items(orderItems)   // CASCADE ALL: OrderItems được lưu cùng Order
                    .build();

            // Bắt buộc thiết lập mối quan hệ liên kết ngược lại dẫn về đơn hàng cha để Hibernate lưu order_id
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrder(order);
            }

            orderRepository.save(order);
            log.info("[PLACE ORDER] Đơn #{} PAID | {} items | {} VNĐ | PT: CHUYỂN KHOẢN",
                    order.getId(), orderItems.size(), finalTotal);

            // ── Bước 6: Xóa giỏ hàng Redis sau khi đặt thành công ─────────────────
            cartGateway.deleteByUserId(usernameOrId);
            log.info("[CART] Đã xóa giỏ hàng Redis của user: {}", usernameOrId);

            // ── Bước 7: Cộng dồn tổng chi tiêu CRM → tự động nâng hạng ───────────
            if (matchedCustomer != null) {
                matchedCustomer.setTotalSpent(
                        matchedCustomer.getTotalSpent() == null
                                ? finalTotal
                                : matchedCustomer.getTotalSpent().add(finalTotal)
                );
                matchedCustomer.updateCustomerTypeBasedOnSpent(); // Tự động nâng hạng
                customerRepository.save(matchedCustomer);
                log.info("[CRM] Đã cộng dồn {} VNĐ → tổng chi tiêu mới: {} | Hạng: {}",
                        finalTotal, matchedCustomer.getTotalSpent(), matchedCustomer.getCustomerType());
            }

            // ── Bước 8: Trả về URL thanh toán VietQR ──────────────────────────────
            return vietQRService.createPaymentUrl(order);

        } catch (UseCaseException e) {
            log.error("[PLACE ORDER ERROR] Lỗi nghiệp vụ đặt hàng: ", e);
            throw e;
        } catch (Exception e) {
            log.error("[PLACE ORDER ERROR] Lỗi hệ thống khi tạo đơn hàng: ", e);
            throw new RuntimeException("Đặt hàng không thành công: " + e.getMessage(), e);
        }
    }

    // Helper: Sinh ngẫu nhiên khách hàng vãng lai mới
    private Customer generateRandomWalkInCustomer() {
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000); // Sinh ngẫu nhiên từ 1000 đến 9999
        String hoName = HO[random.nextInt(HO.length)];
        String demTenName = DEM_TEN[random.nextInt(DEM_TEN.length)];
        String fullName = hoName + " " + demTenName;

        String phone = "";
        boolean isUnique = false;
        // Lặp sinh số điện thoại ngẫu nhiên đến khi không bị trùng khóa unique dưới DB
        while (!isUnique) {
            StringBuilder sb = new StringBuilder("09");
            for (int i = 0; i < 8; i++) {
                sb.append(random.nextInt(10));
            }
            phone = sb.toString();
            if (!customerRepository.existsByPhone(phone)) {
                isUnique = true;
            }
        }

        Customer newCustomer = new Customer();
        newCustomer.setId(UUID.randomUUID().toString());
        newCustomer.setPhone(phone);
        newCustomer.setFullName(fullName);
        newCustomer.setCustomerType("NEW");
        newCustomer.setTotalSpent(BigDecimal.ZERO);
        newCustomer.setNotes("Khách vãng lai tại quầy POS (Tự động sinh)");

        log.info("[CRM AUTO] Đã tự động tạo khách hàng vãng lai mới: Phone={}, Name={}", phone, fullName);
        return customerRepository.save(newCustomer);
    }

    // Helper: Tạo nhanh khách hàng mới khi quầy POS nhập SĐT chưa có trong DB
    private Customer createNewCustomerWithPhone(String phone) {
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000);
        String hoName = HO[random.nextInt(HO.length)];
        String demTenName = DEM_TEN[random.nextInt(DEM_TEN.length)];
        String fullName = hoName + " " + demTenName;

        Customer newCustomer = new Customer();
        newCustomer.setId(UUID.randomUUID().toString());
        newCustomer.setPhone(phone);
        newCustomer.setFullName(fullName);
        newCustomer.setCustomerType("NEW");
        newCustomer.setTotalSpent(BigDecimal.ZERO);
        newCustomer.setNotes("Khách mới đăng ký qua SĐT tại quầy POS");

        log.info("[CRM AUTO] Đã tạo khách hàng mới theo SĐT quầy POS: Phone={}, Name={}", phone, fullName);
        return customerRepository.save(newCustomer);
    }
}