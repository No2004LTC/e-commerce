package ecommerce.example.ecommerce.domain.products;

import java.util.Optional;
import java.util.List;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);
    List<Product> findAll();
    Product persist(Product product);
    void deleteById(ProductId id);
}