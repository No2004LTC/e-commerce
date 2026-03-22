package ecommerce.example.ecommerce.adapter.persistence.User;

import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<User, UserId>, UserRepository {
    @Override
    Optional<User> findByUsername(String username);

    @Override
    Optional<User> findByEmail(String email);

    @Override
    default User persist(User user) {
        return save(user);
    }
    // findById đã có sẵn trong JpaRepository
}