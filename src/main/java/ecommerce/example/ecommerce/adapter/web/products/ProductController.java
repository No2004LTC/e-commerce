package ecommerce.example.ecommerce.adapter.web.products;

import ecommerce.example.ecommerce.application.dto.Product;
import ecommerce.example.ecommerce.application.products.CreateProductUseCase;
import ecommerce.example.ecommerce.application.products.ImportProductsUseCase; // Import thêm UseCase mới
import ecommerce.example.ecommerce.application.products.ProductRequest;
import ecommerce.example.ecommerce.application.products.UploadProductImageUseCase;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
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
    private final ImportProductsUseCase importProductsUseCase; // Inject thêm UseCase xử lý Excel

    @GetMapping("/me")
    public ResponseEntity<List<Product>> getMyProducts(org.springframework.security.core.Authentication authentication) {
        String userId = authentication.getName(); 
        
        List<Product> myProducts = productRepository.findAll().stream()
            .filter(entity -> userId.equals(entity.getOwnerId()))
            .map(entity -> new Product(
                entity.getId().getValue(),
                entity.getOwnerId(), 
                entity.getProductCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getProductImageUrl(),
                entity.getPrice(),
                entity.getStockQuantity(),
                entity.getSoldQuantity(),
                entity.getWarehouse(),
                entity.getSupplier(),
                entity.getStatus() 
            ))
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(myProducts);
    }

    @PostMapping
    public ResponseEntity<Product> create(
            @RequestBody ProductRequest request, 
            Authentication authentication) {
        
        String userId = authentication.getName(); 
        return ResponseEntity.ok(createProductUseCase.execute(request, userId));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        String url = uploadProductImageUseCase.execute(id, file);
        return ResponseEntity.ok(Map.of("productImageUrl", url));
    }

    // =========================================================================
    // TÍNH NĂNG MỚI: IMPORT HÀNG LOẠT SẢN PHẨM TỪ FILE EXCEL CHO CHI NHÁNH/SHOP
    // =========================================================================
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File Excel trống, vui lòng chọn tệp hợp lệ!"));
        }

        // Tự động nhận diện mã Shop lớn/Chi nhánh đang thao tác qua token bảo mật
        String shopOwnerId = authentication.getName();

        try {
            int importedCount = importProductsUseCase.execute(file, shopOwnerId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Nhập danh mục hàng hóa từ Excel thành công! Đã thêm " + importedCount + " mặt hàng mới."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi đọc dữ liệu tệp Excel: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id, 
            @RequestBody ProductRequest request) {
        
        ecommerce.example.ecommerce.domain.products.Product entity = productRepository.findById(
            ecommerce.example.ecommerce.domain.products.ProductId.fromString(id)
        ).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        
        if (request.name() != null) entity.setName(request.name());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.price() != null && request.price().compareTo(BigDecimal.ZERO) > 0) entity.setPrice(request.price());
        if (request.stockQuantity() != null && request.stockQuantity() >= 0) entity.setStockQuantity(request.stockQuantity());
        if (request.warehouse() != null) entity.setWarehouse(request.warehouse());
        
        if (request.status() != null) {
            entity.setStatus(request.status()); 
        }
        
        productRepository.save(entity);
        
        return ResponseEntity.ok(new Product(
            entity.getId().getValue(),
            entity.getOwnerId(), 
            entity.getProductCode(),
            entity.getName(),
            entity.getDescription(),
            entity.getProductImageUrl(),
            entity.getPrice(),
            entity.getStockQuantity(),
            entity.getSoldQuantity(),
            entity.getWarehouse(),
            entity.getSupplier(),
            entity.getStatus() 
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productRepository.deleteById(ProductId.fromString(id));
        return ResponseEntity.noContent().build();
    }
}