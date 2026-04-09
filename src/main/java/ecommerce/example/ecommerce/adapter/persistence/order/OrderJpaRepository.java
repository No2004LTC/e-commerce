package ecommerce.example.ecommerce.adapter.persistence.order;

import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, String>, OrderRepository {

    // Spring Data JPA sẽ tự động cung cấp triển khai cho các phương thức này 
    // dựa trên quy tắc đặt tên (Query Methods).

    @Override
    List<Order> findByBuyerId(String buyerId);

    @Override
    List<Order> findBySellerId(String sellerId);
}