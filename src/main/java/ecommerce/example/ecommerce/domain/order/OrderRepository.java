package ecommerce.example.ecommerce.domain.order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    
    Order save(Order order); 
    
    Optional<Order> findById(String id);
    List<Order> findByBuyerId(String buyerId);
    List<Order> findBySellerId(String sellerId);
}