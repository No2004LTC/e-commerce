package ecommerce.example.ecommerce.adapter.web.products;

import ecommerce.example.ecommerce.application.dto.Product;
import ecommerce.example.ecommerce.application.products.CreateProductUseCase;
import ecommerce.example.ecommerce.application.products.ImportProductsUseCase;
import ecommerce.example.ecommerce.application.products.ProductRequest;
import ecommerce.example.ecommerce.application.products.UploadProductImageUseCase;
import ecommerce.example.ecommerce.domain.products.ProductId;
import ecommerce.example.ecommerce.domain.products.ProductRepository;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import ecommerce.example.ecommerce.infrastructure.config.MinioProperties;
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
    private final ImportProductsUseCase importProductsUseCase;
    private final MinioProperties minioProperties;
    private final UserRepository userRepository;
    private final ecommerce.example.ecommerce.domain.Category.CategoryRepository categoryRepository;

    private String resolveUserId(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByUsername(auth.getName()))
                .map(u -> u.getId().toString())
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại!"));
    }

    // =========================================================================
    // PUBLIC: Lấy sản phẩm AVAILABLE — không cần đăng nhập (POS storefront)
    // Query param: ?branchId={uuid} — nếu có, lọc theo chi nhánh cụ thể
    //              (không có) — trả về tất cả sản phẩm AVAILABLE của toàn hệ thống
    // =========================================================================
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(name = "branchId", required = false) String branchId,
            @RequestParam(name = "includeHidden", required = false, defaultValue = "false") boolean includeHidden) {

        List<ecommerce.example.ecommerce.domain.products.Product> source;

        if (branchId != null && !branchId.isBlank()) {
            // Lọc theo chi nhánh cụ thể (owner_id = branchId)
            source = productRepository.findByOwnerId(branchId);
        } else {
            // Không có branchId → lấy toàn bộ
            source = productRepository.findAll();
        }

        List<Product> products = source.stream()
            .filter(entity -> includeHidden || "AVAILABLE".equalsIgnoreCase(entity.getStatus()))
            .map(this::toDto)
            .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }

    // =========================================================================
    // PUBLIC: Lấy chi tiết một sản phẩm theo ID — không cần đăng nhập
    // =========================================================================
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return productRepository.findById(ProductId.fromString(id))
            .map(entity -> ResponseEntity.ok(toDto(entity)))
            .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================================
    // AUTHENTICATED: Lấy danh sách sản phẩm của chính mình (theo ownerId)
    // =========================================================================
    @GetMapping("/me")
    public ResponseEntity<List<Product>> getMyProducts(Authentication authentication) {
        String userId = resolveUserId(authentication);

        List<Product> myProducts = productRepository.findByOwnerId(userId).stream()
            .map(entity -> toDto(entity))
            .collect(Collectors.toList());

        return ResponseEntity.ok(myProducts);
    }

    // =========================================================================
    // AUTHENTICATED: Tạo mới sản phẩm
    // =========================================================================
    @PostMapping
    public ResponseEntity<Product> create(
            @RequestBody ProductRequest request,
            Authentication authentication) {
        String userId = resolveUserId(authentication);
        String targetOwnerId = (request.ownerId() != null && !request.ownerId().isBlank()) ? request.ownerId() : userId;
        return ResponseEntity.ok(createProductUseCase.execute(request, targetOwnerId));
    }

    // =========================================================================
    // AUTHENTICATED: Upload ảnh sản phẩm lên MinIO
    // =========================================================================
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        String url = uploadProductImageUseCase.execute(id, file);
        return ResponseEntity.ok(Map.of("productImageUrl", url));
    }

    // =========================================================================
    // AUTHENTICATED: Import hàng loạt sản phẩm từ file Excel
    // =========================================================================
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importExcel(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "File Excel trống, vui lòng chọn tệp hợp lệ!"));
        }

        String shopOwnerId = resolveUserId(authentication);

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

    // =========================================================================
    // AUTHENTICATED: Import hàng loạt sản phẩm từ file Excel (theo branchId)
    // =========================================================================
    @PostMapping(value = "/{branchId}/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importProducts(
            @PathVariable String branchId,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "File Excel trống, vui lòng chọn tệp hợp lệ!"));
        }

        try {
            int importedCount = importProductsUseCase.execute(file, branchId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Import danh sách sản phẩm thành công! Đã thêm " + importedCount + " mặt hàng mới."
            ));
        } catch (ecommerce.example.ecommerce.application.common.UseCaseException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Đã xảy ra lỗi khi đọc dữ liệu tệp Excel: " + e.getMessage()
            ));
        }
    }

    // =========================================================================
    // AUTHENTICATED: Cập nhật thông tin sản phẩm
    // =========================================================================
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @RequestBody ProductRequest request,
            @RequestParam(name = "branchId", required = false) String branchId) {

        ecommerce.example.ecommerce.domain.products.Product entity = productRepository.findById(
            ProductId.fromString(id)
        ).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        if (request.name() != null) entity.setName(request.name());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.price() != null && request.price().compareTo(BigDecimal.ZERO) > 0)
            entity.setPrice(request.price());
        if (request.stockQuantity() != null && request.stockQuantity() >= 0)
            entity.setStockQuantity(request.stockQuantity());
        if (request.warehouse() != null) entity.setWarehouse(request.warehouse());
        if (request.supplier() != null) entity.setSupplier(request.supplier());
        if (request.status() != null) entity.setStatus(request.status());
        
        if (request.categoryId() != null) {
            if (request.categoryId().isBlank()) {
                entity.setCategory(null);
                entity.setCategoryId(null);
            } else {
                categoryRepository.findById(new ecommerce.example.ecommerce.domain.Category.CategoryId(request.categoryId()))
                        .ifPresent(category -> {
                            entity.setCategory(category);
                            entity.setCategoryId(request.categoryId());
                        });
            }
        }

        // Gán ownerId bằng branchId từ query param nếu có, ngược lại dùng ownerId từ body
        String targetOwnerId = (branchId != null && !branchId.isBlank()) ? branchId : request.ownerId();
        if (targetOwnerId != null && !targetOwnerId.isBlank()) {
            entity.setOwnerId(targetOwnerId);
        }

        productRepository.save(entity);

        return ResponseEntity.ok(toDto(entity));
    }

    // =========================================================================
    // AUTHENTICATED: Xóa sản phẩm
    // =========================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productRepository.deleteById(ProductId.fromString(id));
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Helper: Chuyển đổi Domain Entity → DTO với MinIO URL đầy đủ
    // productImageUrl đã là full URL (lưu từ MinioStorageService.uploadFile)
    // Nếu chỉ lưu path tương đối, tự động build URL đầy đủ ở đây
    // =========================================================================
    private Product toDto(ecommerce.example.ecommerce.domain.products.Product entity) {
        String imageUrl = resolveImageUrl(entity.getProductImageUrl());
        ecommerce.example.ecommerce.application.dto.CategoryDTO categoryDto = null;
        if (entity.getCategory() != null) {
            categoryDto = ecommerce.example.ecommerce.application.dto.CategoryDTO.builder()
                .id(entity.getCategory().getId().getValue())
                .name(entity.getCategory().getName())
                .slug(entity.getCategory().getSlug())
                .parentId(entity.getCategory().getParentId())
                .build();
        }
        return new Product(
            entity.getId().getValue(),
            entity.getOwnerId(),
            entity.getProductCode(),
            entity.getName(),
            entity.getDescription(),
            imageUrl,
            entity.getPrice(),
            entity.getStockQuantity(),
            entity.getSoldQuantity(),
            entity.getWarehouse(),
            entity.getSupplier(),
            entity.getStatus(),
            categoryDto
        );
    }

    /**
     * Đảm bảo productImageUrl luôn là URL CDN đầy đủ trỏ tới MinIO.
     * - Nếu đã là URL đầy đủ (http/https): trả về nguyên si.
     * - Nếu là path tương đối (ví dụ: "products/uuid_abc.jpg"): build URL đầy đủ.
     * - Nếu null: trả về null (Frontend xử lý placeholder).
     */
    private String resolveImageUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
            return rawUrl; // Đã là URL đầy đủ từ MinioStorageService
        }
        // Tự động build URL đầy đủ nếu chỉ lưu path tương đối
        return String.format("%s/%s/%s",
            minioProperties.getUrl(),
            minioProperties.getBucket(),
            rawUrl
        );
    }
}