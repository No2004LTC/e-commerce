package ecommerce.example.ecommerce.application.Wishlist;

import ecommerce.example.ecommerce.domain.Wishlist.Wishlist;
import ecommerce.example.ecommerce.domain.Wishlist.WishlistId;
import ecommerce.example.ecommerce.domain.Wishlist.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;

    @Transactional
public boolean toggleWishlist(String userId, String productId) {
    boolean exists = wishlistRepository.existsByUserIdAndProductId(userId, productId);
    
    if (exists) {
        wishlistRepository.delete(userId, productId);
        return false; // Trả về false nghĩa là vừa XÓA
    } else {
        Wishlist wishlist = new Wishlist(
            new WishlistId(UUID.randomUUID().toString()),
            userId,
            productId,
            null
        );
        wishlistRepository.save(wishlist);
        return true; // Trả về true nghĩa là vừa THÊM
    }
}
}