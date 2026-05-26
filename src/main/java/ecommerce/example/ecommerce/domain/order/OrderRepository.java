package ecommerce.example.ecommerce.domain.order;

import java.util.List;
import java.util.Optional;

/**
 * Domain interface cho Order Repository.
 * Tầng Application (UseCases) chỉ giao tiếp qua interface này — Clean Architecture.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(String id);

    List<Order> findByBuyerId(String buyerId);

    List<Order> findBySellerId(String sellerId);

    /**
     * Lấy dữ liệu doanh thu thô theo từng chi nhánh (cho biểu đồ tròn).
     * @param parentId UUID của cửa hàng lớn (root)
     * @return List Object[]: [sellerId, totalRevenue]
     */
    List<Object[]> findBranchesRevenue(String parentId);

    /**
     * Lấy dữ liệu doanh thu theo tuần trong tháng hiện tại (cho biểu đồ đường/cột).
     * @param parentId UUID của cửa hàng lớn (root)
     * @return List Object[]: [yearWeek (int), totalRevenue (BigDecimal)]
     */
    List<Object[]> findWeeklyRevenue(String parentId);

    /**
     * Lấy tất cả đơn hàng của một user theo sellerId (cho export Excel).
     */
    List<Order> findAll();

    /**
     * Tìm kiếm đơn hàng theo chi nhánh hoặc toàn chuỗi của cửa hàng lớn.
     */
    List<Order> findByBranchOrParentChain(String branchId);
}