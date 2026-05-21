
package ecommerce.example.ecommerce.domain.customers;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    List<Customer> findAll();
    Optional<Customer> findById(String id);
    Optional<Customer> findByPhone(String phone);
    Customer save(Customer customer);
    void deleteById(String id);
}