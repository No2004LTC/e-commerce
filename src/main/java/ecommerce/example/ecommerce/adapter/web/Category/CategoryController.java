package ecommerce.example.ecommerce.adapter.web.Category;

import ecommerce.example.ecommerce.application.Category.CategoryService;
import ecommerce.example.ecommerce.application.dto.CategoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Lấy cây danh mục (Public - Không cần Token)
    @GetMapping
    public List<CategoryDTO> getTree() {
        return categoryService.getCategoryTree();
    }

    // Tạo danh mục (Private - Cần Token)
    @PostMapping
    public ResponseEntity<String> create(@RequestBody CategoryDTO dto) {
        categoryService.create(dto);
        return ResponseEntity.ok("Tạo danh mục thành công!");
    }

    // Sửa danh mục (Private - Cần Token)
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable String id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto);
        return ResponseEntity.ok("Cập nhật thành công!");
    }

    // Xóa danh mục (Private - Cần Token)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        categoryService.delete(id);
        return ResponseEntity.ok("Xóa danh mục thành công!");
    }
}