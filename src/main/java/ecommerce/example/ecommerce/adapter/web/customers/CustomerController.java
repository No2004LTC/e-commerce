package ecommerce.example.ecommerce.adapter.web.customers;

import ecommerce.example.ecommerce.application.dto.CustomerResponse;
import ecommerce.example.ecommerce.application.customers.ManageCustomerUseCase;
import ecommerce.example.ecommerce.domain.customers.CustomerRepository;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import ecommerce.example.ecommerce.domain.user.User;
import org.springframework.security.core.Authentication;
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
    private final UserRepository userRepository;

    // 1. READ ALL: Lấy toàn bộ danh sách khách hàng phục vụ trang quản trị CRM
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers(
            @RequestParam(name = "branchId", required = false) String branchId,
            Authentication auth) {
        
        User user = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByUsername(auth.getName()))
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại: " + auth.getName()));

        String targetBranchId = branchId;
        boolean isBranchOrStaff = user.getRole() != null && 
                ("ROLE_BRANCH".equalsIgnoreCase(user.getRole().getName()) || 
                 "ROLE_STAFF".equalsIgnoreCase(user.getRole().getName()));

        if (isBranchOrStaff) {
            targetBranchId = user.getId().toString();
        } else if (user.getRole() != null && "ROLE_SHOP_OWNER".equalsIgnoreCase(user.getRole().getName())) {
            if (targetBranchId == null || targetBranchId.isBlank()) {
                targetBranchId = user.getId().toString();
            }
        }

        boolean isAdmin = user.getRole() != null && "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getName());
        final String finalTargetBranchId = targetBranchId;
        final boolean finalIsAdmin = isAdmin;

        List<CustomerResponse> list = customerRepository.findAll().stream()
                .filter(c -> {
                    if (finalIsAdmin && (finalTargetBranchId == null || finalTargetBranchId.isBlank())) {
                        return true;
                    }
                    if (finalTargetBranchId == null || finalTargetBranchId.isBlank()) {
                        return true;
                    }
                    if (c.getBranchId() == null) {
                        return false;
                    }
                    if (c.getBranchId().equalsIgnoreCase(finalTargetBranchId)) {
                        return true;
                    }
                    var ownerOpt = userRepository.findById(new ecommerce.example.ecommerce.domain.user.UserId(c.getBranchId()));
                    if (ownerOpt.isPresent()) {
                        User branchUser = ownerOpt.get();
                        if (finalTargetBranchId.equalsIgnoreCase(branchUser.getParentId())) {
                            return true;
                        }
                    }
                    return false;
                })
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
    public ResponseEntity<CustomerResponse> create(
            @RequestBody Map<String, String> request,
            Authentication auth) {
        User user = userRepository.findByEmail(auth.getName())
                .or(() -> userRepository.findByUsername(auth.getName()))
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại: " + auth.getName()));

        String branchId = user.getId().toString();

        return ResponseEntity.ok(manageCustomerUseCase.createCustomer(
                request.get("phone"),
                request.get("fullName"),
                request.get("notes"),
                branchId
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