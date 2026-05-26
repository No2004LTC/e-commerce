package ecommerce.example.ecommerce.domain.products;

import java.util.List;
import java.util.Optional;

/**
 * Domain interface cho Product Repository.
 * Tầng Application chỉ được phép giao tiếp qua interface này.
 */
public interface ProductRepository {
    Optional<Product> findById(ProductId id);

    /** Lấy tất cả sản phẩm (không lọc) */
    List<Product> findAll();

    /**
     * Tìm sản phẩm theo ownerId (branchId) — hỗ trợ hiển thị phân cấp multi-tenant.
     * @param ownerId UUID dạng chuỗi của chi nhánh/cửa hàng sở hữu sản phẩm
     */
    List<Product> findByOwnerId(String ownerId);

    Product persist(Product product);
    void deleteById(ProductId id);
    Product save(Product product);
}