package ecommerce.example.ecommerce.infrastructure.persistence;

import ecommerce.example.ecommerce.application.Cart.CartGateway;
import ecommerce.example.ecommerce.domain.Cart.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisCartGatewayImpl implements CartGateway {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "cart:";

    @Override
    public void save(Cart cart) {
        
        redisTemplate.opsForValue().set(KEY_PREFIX + cart.getUserId(), cart, 7, TimeUnit.DAYS);
    }

    @Override
    public Optional<Cart> findByUserId(String userId) {
        Object data = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return Optional.ofNullable((Cart) data);
    }

   
    @Override
    public void deleteByUserId(String userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}