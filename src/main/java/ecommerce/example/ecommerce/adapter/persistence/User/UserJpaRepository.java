package ecommerce.example.ecommerce.adapter.persistence.User;

import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository cho User entity.
 * Implements cả UserRepository domain interface (pattern tắt cho User).
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, UserId>, UserRepository {

    @Override
    Optional<User> findByUsername(String username);

    @Override
    Optional<User> findByEmail(String email);

    @Override
    Optional<User> findById(UserId id);

    /**
     * Truy vấn tất cả chi nhánh con theo parentId của cửa hàng lớn.
     * Spring Data tự động sinh câu SQL: WHERE parent_id = ?
     */
    @Override
    List<User> findByParentId(String parentId);

    @Override
    default User persist(User user) {
        return save(user);
    }
}