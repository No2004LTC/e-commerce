package ecommerce.example.ecommerce.adapter.web.customers;

import ecommerce.example.ecommerce.application.dto.CustomerResponse;
import ecommerce.example.ecommerce.application.customers.ManageCustomerUseCase;
import ecommerce.example.ecommerce.domain.customers.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final ManageCustomerUseCase manageCustomerUseCase;

    // 1. READ ALL: Lấy toàn bộ danh sách khách hàng phục vụ trang quản trị CRM
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> list = customerRepository.findAll().stream()
                .map(manageCustomerUseCase::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // 2. SEARCH BY PHONE: Tra cứu nhanh thông tin khách bằng SĐT tại quầy POS để lấy % chiết khấu
    @GetMapping("/search")
public ResponseEntity<CustomerResponse> getCustomerByPhone(@RequestParam String phone) {
        return customerRepository.findByPhone(phone)
                .map(entity -> ResponseEntity.ok(manageCustomerUseCase.mapToResponse(entity))) 
                .orElse(ResponseEntity.notFound().build());
}

    // 3. CREATE: Thêm mới hồ sơ khách hàng
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(manageCustomerUseCase.createCustomer(
                request.get("phone"),
                request.get("fullName"),
                request.get("notes")
        ));
    }

    // 4. UPDATE: Chỉnh sửa hồ sơ hoặc cập nhật ghi chú
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable String id,
            @RequestBody Map<String, Object> request) {
        
        String fullName = (String) request.get("fullName");
        String notes = (String) request.get("notes");
        java.math.BigDecimal additionalSpent = request.get("additionalSpent") != null ? 
            new java.math.BigDecimal(request.get("additionalSpent").toString()) : java.math.BigDecimal.ZERO;

        return ResponseEntity.ok(manageCustomerUseCase.updateCustomer(id, fullName, additionalSpent, notes));
    }

    // 5. DELETE: Xóa dữ liệu khách hàng khỏi danh sách
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        customerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}