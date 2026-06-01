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
    // ANALYTICS: Doanh thu theo từng chi nhánh — LEFT JOIN users
    // Chỉ tính đơn hàng có status = 'PAID'
    // Trả về Object[] mỗi phần tử: [branchLabel (String), totalRevenue (BigDecimal)]
    //
    // LEFT JOIN khớp seller_id (UUID / username / email) sang bảng users.
    // COALESCE ưu tiên: username → email → seller_id (fallback thô)
    // WHERE bao phủ: chính chủ chuỗi (ownerId) và tất cả chi nhánh con (parent_id)
    // GROUP BY dùng ĐÚNG biểu thức COALESCE — tránh lỗi ONLY_FULL_GROUP_BY MySQL
    // =========================================================================
    @Query(value = """
        SELECT u.username AS branch_label,
               SUM(CAST(o.total_amount AS DECIMAL(20,2))) AS branch_revenue
        FROM orders o
        INNER JOIN users u ON (o.seller_id = u.id OR o.seller_id = u.username OR o.seller_id = u.email)
        WHERE o.status IN ('PAID', 'DELIVERED')
          AND u.parent_id = :ownerId
        GROUP BY u.username
        ORDER BY branch_revenue DESC
        """, nativeQuery = true)
    List<Object[]> findBranchesRevenue(@Param("ownerId") String ownerId);

    // =========================================================================
    // ANALYTICS: Doanh thu theo tuần (Weekly grouping) — ONLY_FULL_GROUP_BY safe
    // Lấy 12 tuần gần nhất, tính cho toàn bộ chi nhánh thuộc chuỗi của parentId
    // Trả về: [weekLabel ("Tuần XX - YYYY"), totalRevenue]
    //
    // LEFT JOIN để bao phủ seller_id lưu dạng UUID hoặc username.
    // GROUP BY dùng biểu thức CONCAT trùng với SELECT — tuyệt đối an toàn với MySQL strict mode.
    // =========================================================================
    @Query(value = """
        SELECT DATE_FORMAT(o.created_at, '%d/%m')                                  AS week_label,
               SUM(o.total_amount)                                                 AS total_revenue
        FROM   orders o
        LEFT JOIN users u
               ON  u.id       = o.seller_id
                OR u.username = o.seller_id
                OR u.email    = o.seller_id
        WHERE  o.status IN ('PAID', 'DELIVERED')
          AND  o.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
          AND (
               u.id        = :parentId
            OR u.parent_id = :parentId
            OR o.seller_id = :parentId
          )
        GROUP BY DATE_FORMAT(o.created_at, '%d/%m'),
                 DATE(o.created_at)
        ORDER BY DATE(o.created_at) ASC
        """, nativeQuery = true)
    List<Object[]> findWeeklyRevenue(@Param("parentId") String parentId);

    // =========================================================================
    // Lấy toàn bộ đơn hàng của chi nhánh hoặc toàn chuỗi của cửa hàng lớn
    // LEFT JOIN để khớp seller_id dưới mọi dạng (UUID / username / email)
    // Sắp xếp theo ngày tạo (created_at) giảm dần
    // =========================================================================
    @Query(value = """
        SELECT DISTINCT o.*
        FROM   orders o
        LEFT JOIN users u
               ON  u.id       = o.seller_id
                OR u.username = o.seller_id
                OR u.email    = o.seller_id
        WHERE (
               u.id        = :branchId
            OR u.parent_id = :branchId
            OR o.seller_id = :branchId
        )
        ORDER BY o.created_at DESC
        """, nativeQuery = true)
    List<Order> findByBranchOrParentChain(@Param("branchId") String branchId);
}