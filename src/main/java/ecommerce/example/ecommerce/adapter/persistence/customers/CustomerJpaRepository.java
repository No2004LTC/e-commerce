package ecommerce.example.ecommerce.adapter.persistence.customers;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, String> {
    Optional<CustomerEntity> findByPhone(String phone);
    boolean existsByPhone(String phone);
}