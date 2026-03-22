package ecommerce.example.ecommerce.domain.user;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UserId id);
    User persist(User user);
}