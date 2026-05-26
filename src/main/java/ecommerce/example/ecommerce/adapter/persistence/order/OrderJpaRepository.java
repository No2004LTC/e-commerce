package ecommerce.example.ecommerce.adapter.persistence.order;

import ecommerce.example.ecommerce.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository thuần túy cho entity Order.
 * Chỉ chịu trách nhiệm truy vấn dữ liệu JPA — KHÔNG implements domain interface.
 * OrderPersistenceAdapter đóng vai trò cầu nối sang domain OrderRepository.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, String> {

    List<Order> findByBuyerId(String buyerId);

    List<Order> findBySellerId(String sellerId);

    // =========================================================================
    // ANALYTICS: Doanh thu theo từng chi nhánh (Group by sellerId)
    // Chỉ tính đơn hàng có status = 'PAID'
    // Trả về Object[] mỗi phần tử: [sellerId (String), totalRevenue (BigDecimal)]
    // =========================================================================
    @Query(value = """
        SELECT o.seller_id, SUM(o.total_amount)
        FROM orders o
        WHERE o.status = 'PAID'
          AND (
              o.seller_id IN (
                  SELECT u.id FROM users u WHERE u.parent_id = :parentId
              )
              OR o.seller_id IN (
                  SELECT u.username FROM users u WHERE u.parent_id = :parentId
              )
              OR o.seller_id IN (
                  SELECT u.full_name FROM users u WHERE u.parent_id = :parentId
              )
          )
        GROUP BY o.seller_id
        ORDER BY SUM(o.total_amount) DESC
        """, nativeQuery = true)
    List<Object[]> findBranchesRevenue(@Param("parentId") String parentId);

    // =========================================================================
    // ANALYTICS: Doanh thu theo ngày trong tháng hiện tại (Daily grouping)
    // Tính cho toàn bộ chi nhánh thuộc chuỗi của parentId
    // =========================================================================
    @Query(value = """
        SELECT DATE_FORMAT(o.created_at, '%Y-%m-%d') AS order_date,
               SUM(o.total_amount)                   AS total_revenue
        FROM orders o
        WHERE o.status = 'PAID'
          AND YEAR(o.created_at)  = YEAR(CURDATE())
          AND MONTH(o.created_at) = MONTH(CURDATE())
          AND (
              o.seller_id = :parentId
              OR o.seller_id IN (
                  SELECT u.id FROM users u WHERE u.parent_id = :parentId
              )
              OR o.seller_id IN (
                  SELECT u.username FROM users u WHERE u.parent_id = :parentId
              )
              OR o.seller_id IN (
                  SELECT u.full_name FROM users u WHERE u.parent_id = :parentId
              )
          )
        GROUP BY DATE_FORMAT(o.created_at, '%Y-%m-%d')
        ORDER BY order_date ASC
        """, nativeQuery = true)
    List<Object[]> findWeeklyRevenue(@Param("parentId") String parentId);

    // =========================================================================
    // Lấy toàn bộ đơn hàng của chi nhánh hoặc toàn chuỗi của cửa hàng lớn
    // Sắp xếp theo ngày tạo (created_at) giảm dần
    // =========================================================================
    @Query(value = """
        SELECT o.* FROM orders o
        WHERE o.seller_id = :branchId
           OR o.seller_id IN (
               SELECT u.username FROM users u WHERE u.id = :branchId OR u.parent_id = :branchId
           )
           OR o.seller_id IN (
               SELECT u.full_name FROM users u WHERE u.id = :branchId OR u.parent_id = :branchId
           )
           OR o.seller_id IN (
               SELECT u.id FROM users u WHERE u.id = :branchId OR u.parent_id = :branchId
           )
        ORDER BY o.created_at DESC
        """, nativeQuery = true)
    List<Order> findByBranchOrParentChain(@Param("branchId") String branchId);
}