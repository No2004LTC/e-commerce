package ecommerce.example.ecommerce.adapter.persistence.customers;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Data
public class CustomerEntity {
    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String phone;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "customer_type")
    private String customerType;
    
    @Column(name = "total_spent")
    private BigDecimal totalSpent;
    
    private String notes;
}