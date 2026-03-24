package ecommerce.example.ecommerce.adapter.web.products;

import ecommerce.example.ecommerce.application.dto.Product; // Import DTO
import ecommerce.example.ecommerce.application.products.CreateProductUseCase;
import ecommerce.example.ecommerce.application.products.ProductRequest;
import ecommerce.example.ecommerce.application.products.UploadProductImageUseCase;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final CreateProductUseCase createProductUseCase;
    private final UploadProductImageUseCase uploadProductImageUseCase;

    // 1. Lấy danh sách sản phẩm (Trả về DTO)
    @GetMapping
    public List<Product> getAll() {
        return productRepository.findAll().stream()
            .map(entity -> new Product(
                entity.getId().getValue(),
                entity.getProductCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getProductImageUrl(),
                entity.getPrice(),
                entity.getStockQuantity(),
                entity.getSoldQuantity(),
                entity.getWarehouse(),
                entity.getSupplier()
            ))
            .collect(Collectors.toList());
    }

    // 2. Tạo sản phẩm mới (Gọi qua UseCase)
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(createProductUseCase.execute(request));
    }

    // 3. Upload ảnh cho sản phẩm
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        String url = uploadProductImageUseCase.execute(id, file);
        return ResponseEntity.ok(Map.of("productImageUrl", url));
    }

    // 4. Xóa sản phẩm
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productRepository.deleteById(ProductId.fromString(id));
        return ResponseEntity.noContent().build();
    }
}