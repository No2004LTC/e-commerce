package ecommerce.example.ecommerce.adapter.persistence.User;

import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository - Implements the domain UserRepository interface
 * This is the adapter that bridges between domain and persistence layer
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User,UserId>, UserRepository {
    @Override
    Optional<User> findByUsername(String username);

    @Override
    Optional<User> findByEmail(String email);
    @Override
    Optional<User> findById(UserId id);
    
}
