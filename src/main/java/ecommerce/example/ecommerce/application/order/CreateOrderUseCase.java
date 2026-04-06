package ecommerce.example.ecommerce.application.order;
import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.application.products.ProductService;
import ecommerce.example.ecommerce.domain.Cart.Cart;
import ecommerce.example.ecommerce.domain.Cart.CartItem;
import ecommerce.example.ecommerce.domain.order.Order;          
import ecommerce.example.ecommerce.domain.order.OrderItem;      
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import ecommerce.example.ecommerce.domain.products.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CreateOrderUseCase {
    private final CartGateway cartGateway;
    private final ProductService productService;
    private final OrderRepository orderRepository; 

    @Transactional
    public List<Order> execute(String buyerId) {
        // 1. Lấy giỏ hàng từ Redis
        Cart cart = cartGateway.findByUserId(buyerId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống!"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng không có sản phẩm nào.");
        }

        // 2. Gom nhóm sản phẩm theo Seller (Vì 1 giỏ hàng có thể mua của nhiều Shop)
        // Đây là kỹ thuật quan trọng trong C2C
        Map<String, List<CartItem>> itemsBySeller = groupItemsBySeller(cart.getItems());

        List<Order> createdOrders = new ArrayList<>();

        for (Map.Entry<String, List<CartItem>> entry : itemsBySeller.entrySet()) {
            String sellerId = entry.getKey();
            List<CartItem> sellerItems = entry.getValue();

            // 3. Tạo Entity Order
            Order order = Order.builder()
                    .id(UUID.randomUUID().toString())
                    .buyerId(buyerId)
                    .sellerId(sellerId)
                    .status("PENDING")
                    .items(mapToOrderItems(sellerItems))
                    .totalAmount(calculateTotal(sellerItems))
                    .build();

            // 4. Lưu vào Database
            orderRepository.save(order);
            createdOrders.add(order);
        }

        // 5. Xóa giỏ hàng Redis sau khi đã tạo Order thành công
        cartGateway.deleteByUserId(buyerId);

        return createdOrders;
    }

    private Map<String, List<CartItem>> groupItemsBySeller(List<CartItem> items) {
        return items.stream().collect(Collectors.groupingBy(item -> {
            // Lấy ownerId từ Database cho mỗi sản phẩm trong giỏ
            return productService.findById(ProductId.fromString(item.getProductId()))
                    .get().getOwnerId();
        }));
    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private List<OrderItem> mapToOrderItems(List<CartItem> items) {
        return items.stream().map(i -> new OrderItem(i.getProductId(), i.getName(), i.getPrice(), i.getQuantity())).toList();
    }
}
