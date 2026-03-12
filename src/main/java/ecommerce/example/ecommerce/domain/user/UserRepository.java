package ecommerce.example.ecommerce.domain.user;

import java.util.Optional;

/**
 * Port Out: repository interface defined in the domain layer.
 */
public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    User save(User user);
}
