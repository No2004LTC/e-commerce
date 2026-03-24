package ecommerce.example.ecommerce.application.products;

import ecommerce.example.ecommerce.domain.products.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateProductUseCase {
    private final ProductService productService;

    @Transactional
    public ecommerce.example.ecommerce.application.dto.Product execute(ProductRequest request) {
        // 1. Dùng đường dẫn đầy đủ cho Domain Entity để tránh xung đột với DTO
        ecommerce.example.ecommerce.domain.products.Product entity = new ecommerce.example.ecommerce.domain.products.Product();
        
        entity.setId(ProductId.generate());
        entity.setProductCode("PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setName(request.name());
        entity.setPrice(request.price());
        entity.setDescription(request.description());
        entity.setStockQuantity(request.stockQuantity());
        entity.setWarehouse(request.warehouse());
        entity.setSupplier(request.supplier());
        entity.setSoldQuantity(0);

        // 2. Lưu vào DB qua Service
        ecommerce.example.ecommerce.domain.products.Product saved = productService.save(entity);

        // 3. Trả về DTO (Record Product trong package dto)
        return new ecommerce.example.ecommerce.application.dto.Product(
            saved.getId().getValue(),
            saved.getProductCode(),
            saved.getName(),
            saved.getDescription(),
            saved.getProductImageUrl(),
            saved.getPrice(),
            saved.getStockQuantity(),
            saved.getSoldQuantity(),
            saved.getWarehouse(),
            saved.getSupplier()
        );
    }
}