package ecommerce.example.ecommerce.adapter.persistence.products;

import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository cho Product entity.
 * Chỉ chứa các method JPA — ProductRepository domain interface
 * được implement bởi MySQLProductRepositoryAdapter.
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<Product, ProductId> {

    // Tìm kiếm sản phẩm theo mã hàng hóa
    Optional<Product> findByProductCode(String productCode);

    /**
     * Tìm tất cả sản phẩm thuộc một chi nhánh cụ thể (theo ownerId).
     * Spring Data tự động sinh: WHERE owner_id = ?
     */
    List<Product> findByOwnerId(String ownerId);

    /**
     * Tìm sản phẩm theo ownerId và status (ví dụ chỉ lấy AVAILABLE)
     */
    List<Product> findByOwnerIdAndStatus(String ownerId, String status);

    // =========================================================================
    // LOW STOCK ALERT: Đếm sản phẩm sắp hết hàng (tồn kho < 5) cho toàn chuỗi
    // Quét cả chi nhánh chủ sở hữu lẫn tất cả chi nhánh con (parent_id = :ownerId)
    // =========================================================================
    @Query(value = """
        SELECT COUNT(*) FROM products p
        WHERE p.stock_quantity < 5
          AND (
              p.owner_id = :ownerId
              OR p.owner_id IN (
                  SELECT u.id FROM users u WHERE u.parent_id = :ownerId
              )
              OR p.owner_id IN (
                  SELECT u.username FROM users u WHERE u.parent_id = :ownerId
              )
          )
        """, nativeQuery = true)
    long countLowStockByChain(@Param("ownerId") String ownerId);
}