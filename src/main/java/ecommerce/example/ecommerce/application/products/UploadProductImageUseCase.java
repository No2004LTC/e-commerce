package ecommerce.example.ecommerce.application.products;

import ecommerce.example.ecommerce.domain.products.Product;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import ecommerce.example.ecommerce.infrastructure.minio.MinioStorageService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadProductImageUseCase {

    private final ProductRepository productRepository;
    private final MinioStorageService storageService;

    public UploadProductImageUseCase(ProductRepository productRepository, MinioStorageService storageService) {
        this.productRepository = productRepository;
        this.storageService = storageService;
    }

    @Transactional
    public String execute(String productIdStr, MultipartFile file) {
        if (productIdStr == null || productIdStr.isEmpty()) {
            throw new RuntimeException("Product ID is required");
        }

        try {
            ProductId productId = ProductId.fromString(productIdStr);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productIdStr));

           
            String imageUrl = storageService.uploadFile(file, "products");

            product.setProductImageUrl(imageUrl);
            productRepository.persist(product);

            return imageUrl;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid UUID format: " + productIdStr);
        }
    }
}