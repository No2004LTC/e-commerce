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
    public ecommerce.example.ecommerce.application.dto.Product execute(ProductRequest request, String ownerId) {
        ecommerce.example.ecommerce.domain.products.Product entity = new ecommerce.example.ecommerce.domain.products.Product();
        
        entity.setId(ProductId.generate());
        entity.setOwnerId(ownerId); 
        entity.setProductCode("PROD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        entity.setName(request.name());
        entity.setPrice(request.price());
        entity.setDescription(request.description());
        entity.setStockQuantity(request.stockQuantity());
        entity.setWarehouse(request.warehouse());
        entity.setSupplier(request.supplier());
        entity.setSoldQuantity(0);
        entity.setStatus("AVAILABLE");

        ecommerce.example.ecommerce.domain.products.Product saved = productService.save(entity);

      return new ecommerce.example.ecommerce.application.dto.Product(
                saved.getId().getValue(),
                saved.getOwnerId(),       
                saved.getProductCode(),  
                saved.getName(),         
                saved.getDescription(),  
                saved.getProductImageUrl(),
                saved.getPrice(),        
                saved.getStockQuantity(), 
                saved.getSoldQuantity(),  
                saved.getWarehouse(),     
                saved.getSupplier(),     
                saved.getStatus()         
        );
    }
}