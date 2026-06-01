package ecommerce.example.ecommerce.application.customers;

import ecommerce.example.ecommerce.application.dto.CustomerResponse;
import ecommerce.example.ecommerce.domain.customers.Customer;
import ecommerce.example.ecommerce.domain.customers.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManageCustomerUseCase {

    private final CustomerRepository customerRepository;

    // Nghiệp vụ Tạo mới khách hàng tại quầy POS
    public CustomerResponse createCustomer(String phone, String fullName, String notes, String branchId) {
        customerRepository.findByPhone(phone).ifPresent(c -> {
            throw new RuntimeException("Số điện thoại khách hàng này đã tồn tại trên hệ thống chuỗi!");
        });

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setPhone(phone);
        customer.setFullName(fullName);
        customer.setTotalSpent(BigDecimal.ZERO);
        customer.setNotes(notes);
        customer.setBranchId(branchId);
        customer.updateCustomerTypeBasedOnSpent();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    // Nghiệp vụ Cập nhật thông tin / Cộng dồn tiền tích lũy khi có đơn hàng PAID
    public CustomerResponse updateCustomer(String id, String fullName, BigDecimal additionalSpent, String notes) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu khách hàng!"));

        if (fullName != null) customer.setFullName(fullName);
        if (notes != null) customer.setNotes(notes);
        if (additionalSpent != null && additionalSpent.compareTo(BigDecimal.ZERO) > 0) {
            customer.setTotalSpent(customer.getTotalSpent().add(additionalSpent));
        }
        
        // Tự động tính lại phân bậc hạng và % giảm giá
        customer.updateCustomerTypeBasedOnSpent();

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    // Hàm chuyển đổi ánh xạ sang DTO
    public CustomerResponse mapToResponse(Customer entity) {
        return new CustomerResponse(
            entity.getId(),
            entity.getPhone(),
            entity.getFullName(),
            entity.getCustomerType(),
            entity.getTotalSpent() != null ? entity.getTotalSpent() : BigDecimal.ZERO,
            entity.getDiscountPercentage(), // Đẩy trường tính toán % tự động cho FE
            entity.getNotes(),
            entity.getBranchId()
        );
    }
}