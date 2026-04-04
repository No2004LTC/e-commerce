package ecommerce.example.ecommerce.application.Cart;

import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.domain.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GetCartUseCase {
    private final CartGateway cartGateway;

    public Cart execute(String userId) {
        return cartGateway.findByUserId(userId)
                .orElse(new Cart(userId, new ArrayList<>()));
    }
}