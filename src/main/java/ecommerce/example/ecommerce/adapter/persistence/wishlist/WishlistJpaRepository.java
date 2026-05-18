package ecommerce.example.ecommerce.adapter.persistence.wishlist;

import ecommerce.example.ecommerce.domain.Wishlist.Wishlist;
import ecommerce.example.ecommerce.domain.Wishlist.WishlistId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WishlistJpaRepository extends JpaRepository<Wishlist, WishlistId> {
    List<Wishlist> findByUserId(String userId);
    void deleteByUserIdAndProductId(String userId, String productId);
    boolean existsByUserIdAndProductId(String userId, String productId);
}