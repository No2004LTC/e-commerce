package ecommerce.example.ecommerce.adapter.persistence.products;

import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    // 🌟 SỬA ĐỔI: Truyền thẳng đối tượng ProductId, không cần tách .getValue().toString() nữa
    @Override
    public Optional<Product> findById(ProductId id) {
        return productJpaRepository.findById(id); 
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

    // 🌟 SỬA ĐỔI: Truyền thẳng đối tượng ProductId vào hàm delete của JPA
    @Override
    public void deleteById(ProductId id) {
        productJpaRepository.deleteById(id);
    }
}