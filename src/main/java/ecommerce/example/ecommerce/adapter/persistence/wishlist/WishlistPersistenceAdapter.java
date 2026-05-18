package ecommerce.example.ecommerce.adapter.persistence.wishlist;

import ecommerce.example.ecommerce.domain.Wishlist.Wishlist;
import ecommerce.example.ecommerce.domain.Wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WishlistPersistenceAdapter implements WishlistRepository {
    private final WishlistJpaRepository repository;

    @Override
    public void save(Wishlist wishlist) {
        repository.save(wishlist);
    }

    @Override
    @Transactional
    public void delete(String userId, String productId) {
        repository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    public List<Wishlist> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndProductId(String userId, String productId) {
        return repository.existsByUserIdAndProductId(userId, productId);
    }
}