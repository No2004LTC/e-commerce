package ecommerce.example.ecommerce.domain.user;

import java.util.List;
import java.util.Optional;

/**
 * Domain interface cho User Repository.
 * Tầng Application chỉ được phép giao tiếp qua interface này.
 */
public interface UserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UserId id);
    void deleteById(UserId id);
    User persist(User user);

    /**
     * Tìm tất cả các chi nhánh (branches) thuộc quyền quản lý của một cửa hàng lớn.
     * @param parentId UUID dạng chuỗi của cửa hàng lớn
     * @return Danh sách User chi nhánh có parentId khớp
     */
    List<User> findByParentId(String parentId);
}
