package ecommerce.example.ecommerce.domain.Wishlist;

import java.util.List;

public interface WishlistRepository {
    void save(Wishlist wishlist);
    void delete(String userId, String productId);
    List<Wishlist> findByUserId(String userId);
    boolean existsByUserIdAndProductId(String userId, String productId);
}