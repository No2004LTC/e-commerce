package ecommerce.example.ecommerce.adapter.persistence.category;

import ecommerce.example.ecommerce.domain.Category.Category;
import ecommerce.example.ecommerce.domain.Category.CategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<Category, CategoryId> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentId(String parentId);
}