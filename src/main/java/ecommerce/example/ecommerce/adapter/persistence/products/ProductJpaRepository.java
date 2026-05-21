package ecommerce.example.ecommerce.adapter.persistence.products;

import ecommerce.example.ecommerce.domain.products.Product; // Import lớp Product gốc của bạn
import ecommerce.example.ecommerce.domain.products.ProductId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductJpaRepository extends JpaRepository<Product, ProductId> { 
    
    // Tìm kiếm sản phẩm theo mã hàng hóa
    Optional<Product> findByProductCode(String productCode);
}