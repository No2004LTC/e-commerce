package ecommerce.example.ecommerce.adapter.persistence.products;

import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adapter tầng Persistence cho domain ProductRepository.
 * Cầu nối giữa domain interface ProductRepository và Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class MySQLProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product persist(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    /**
     * Tìm sản phẩm theo ownerId (branchId).
     * Dùng cho: GET /api/products?branchId=xxx
     */
    @Override
    public List<Product> findByOwnerId(String ownerId) {
        return productJpaRepository.findByOwnerId(ownerId);
    }

    @Override
    public void deleteById(ProductId id) {
        productJpaRepository.deleteById(id);
    }
}