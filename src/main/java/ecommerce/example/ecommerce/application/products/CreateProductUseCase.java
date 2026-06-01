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
    private final ecommerce.example.ecommerce.domain.Category.CategoryRepository categoryRepository;

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

        if (request.categoryId() != null && !request.categoryId().isBlank()) {
            categoryRepository.findById(new ecommerce.example.ecommerce.domain.Category.CategoryId(request.categoryId()))
                    .ifPresent(category -> {
                        entity.setCategory(category);
                        entity.setCategoryId(request.categoryId());
                    });
        }

        ecommerce.example.ecommerce.domain.products.Product saved = productService.save(entity);

        ecommerce.example.ecommerce.application.dto.CategoryDTO categoryDto = null;
        if (saved.getCategory() != null) {
            categoryDto = ecommerce.example.ecommerce.application.dto.CategoryDTO.builder()
                .id(saved.getCategory().getId().getValue())
                .name(saved.getCategory().getName())
                .slug(saved.getCategory().getSlug())
                .parentId(saved.getCategory().getParentId())
                .build();
        }

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
                saved.getStatus(),
                categoryDto
        );
    }
}