package ecommerce.example.ecommerce.application.products;

import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public Product save(Product product) {
        return repository.persist(product);
    }

    public Optional<Product> findById(ProductId id) {
        return repository.findById(id);
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public void deleteById(ProductId id) {
        repository.deleteById(id);
    }
    public void decreaseStock(ProductId id, int amount) {
    Product product = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

    if (product.getStockQuantity() < amount) {
        throw new RuntimeException("Sản phẩm " + product.getName() + " đã hết hàng!");
    }

    product.setStockQuantity(product.getStockQuantity() - amount);
    product.setSoldQuantity(product.getSoldQuantity() + amount); // Tăng số lượng đã bán lên
    
    repository.persist(product);
}
}