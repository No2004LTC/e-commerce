package ecommerce.example.ecommerce.adapter.persistence.customers;

import ecommerce.example.ecommerce.domain.customers.Customer;
import ecommerce.example.ecommerce.domain.customers.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MySQLCustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public List<Customer> findAll() {
        return customerJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Customer> findById(String id) {
        return customerJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
        return customerJpaRepository.findByPhone(phone).map(this::toDomain);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return customerJpaRepository.existsByPhone(phone);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = toEntity(customer);
        CustomerEntity saved = customerJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        customerJpaRepository.deleteById(id);
    }

    // Hàm biến đổi JPA Entity thành Domain Model
    private Customer toDomain(CustomerEntity entity) {
        return new Customer(
            entity.getId(),
            entity.getPhone(),
            entity.getFullName(),
            entity.getCustomerType(),
            entity.getTotalSpent(),
            entity.getNotes(),
            entity.getBranchId()
        );
    }

    // Hàm biến đổi Domain Model thành JPA Entity
    private CustomerEntity toEntity(Customer domain) {
        CustomerEntity entity = new CustomerEntity();
        entity.setId(domain.getId());
        entity.setPhone(domain.getPhone());
        entity.setFullName(domain.getFullName());
        entity.setCustomerType(domain.getCustomerType());
        entity.setTotalSpent(domain.getTotalSpent());
        entity.setNotes(domain.getNotes());
        entity.setBranchId(domain.getBranchId());
        return entity;
    }
}