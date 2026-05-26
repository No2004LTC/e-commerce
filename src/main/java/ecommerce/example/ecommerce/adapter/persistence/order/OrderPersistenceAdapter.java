package ecommerce.example.ecommerce.adapter.persistence.order;

import ecommerce.example.ecommerce.domain.order.Order;
import ecommerce.example.ecommerce.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter tầng Persistence cho domain OrderRepository.
 *
 * Vai trò: Cầu nối (bridge) giữa domain interface OrderRepository và
 * JPA implementation OrderJpaRepository, theo mô hình Ports & Adapters.
 */
@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public List<Order> findByBuyerId(String buyerId) {
        return orderJpaRepository.findByBuyerId(buyerId);
    }

    @Override
    public List<Order> findBySellerId(String sellerId) {
        return orderJpaRepository.findBySellerId(sellerId);
    }

    @Override
    public List<Object[]> findBranchesRevenue(String parentId) {
        return orderJpaRepository.findBranchesRevenue(parentId);
    }

    @Override
    public List<Object[]> findWeeklyRevenue(String parentId) {
        return orderJpaRepository.findWeeklyRevenue(parentId);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll();
    }

    @Override
    public List<Order> findByBranchOrParentChain(String branchId) {
        return orderJpaRepository.findByBranchOrParentChain(branchId);
    }
}
