package ecommerce.example.ecommerce.application.Category;

import ecommerce.example.ecommerce.application.dto.CategoryDTO;
import ecommerce.example.ecommerce.domain.Category.Category;
import ecommerce.example.ecommerce.domain.Category.CategoryId;
import ecommerce.example.ecommerce.domain.Category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 1. LẤY CÂY DANH MỤC (READ)
    public List<CategoryDTO> getCategoryTree() {
        List<Category> all = categoryRepository.findAll();
        return all.stream()
                .filter(c -> c.getParentId() == null)
                .map(parent -> mapToDTO(parent, all))
                .collect(Collectors.toList());
    }

    // 2. TẠO MỚI (CREATE)
    @Transactional
    public void create(CategoryDTO dto) {
        Category category = new Category(
            new CategoryId(UUID.randomUUID().toString()),
            dto.getName(),
            dto.getSlug(),
            "Mô tả mặc định",
            dto.getParentId(),
            null
        );
        categoryRepository.save(category);
    }

    // 3. CẬP NHẬT (UPDATE)
    @Transactional
    public void update(String id, CategoryDTO dto) {
        categoryRepository.findById(new CategoryId(id)).ifPresent(category -> {
            category.setName(dto.getName());
            category.setSlug(dto.getSlug());
            category.setParentId(dto.getParentId());
            categoryRepository.save(category);
        });
    }

    // 4. XÓA (DELETE)
    @Transactional
    public void delete(String id) {
        categoryRepository.delete(new CategoryId(id));
    }

    // Hàm đệ quy để chuyển Entity sang DTO lồng nhau
    private CategoryDTO mapToDTO(Category category, List<Category> all) {
        return CategoryDTO.builder()
                .id(category.getId().getValue())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParentId())
                .children(all.stream()
                        .filter(c -> category.getId().getValue().equals(c.getParentId()))
                        .map(child -> mapToDTO(child, all))
                        .collect(Collectors.toList()))
                .build();
    }
}