package ecommerce.example.ecommerce.application.Cart;

import ecommerce.example.ecommerce.domain.Cart.*;
import java.util.Optional;

public interface CartGateway {
    void save(Cart cart);
    Optional<Cart> findByUserId(String userId);
}
