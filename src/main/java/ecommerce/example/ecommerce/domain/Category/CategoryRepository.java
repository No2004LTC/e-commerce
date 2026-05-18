package ecommerce.example.ecommerce.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    void save(Category category);
    Optional<Category> findById(CategoryId id);
    List<Category> findAll();
    List<Category> findByParentId(String parentId);
    Optional<Category> findBySlug(String slug);
    void delete(CategoryId id);
}