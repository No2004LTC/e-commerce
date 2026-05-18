package ecommerce.example.ecommerce.adapter.persistence.category;

import ecommerce.example.ecommerce.domain.Category.Category;
import ecommerce.example.ecommerce.domain.Category.CategoryId;
import ecommerce.example.ecommerce.domain.Category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepository {
    private final CategoryJpaRepository repository;

    @Override
    public void save(Category category) {
        repository.save(category);
    }

    @Override
    public Optional<Category> findById(CategoryId id) {
        return repository.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Category> findByParentId(String parentId) {
        return repository.findByParentId(parentId);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }

    @Override
    public void delete(CategoryId id) {
        repository.deleteById(id);
    }
}