package ecommerce.example.ecommerce.adapter.web.User;

import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/branches")
@RequiredArgsConstructor
@Slf4j
public class AdminBranchController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // GET /api/admin/branches
    @GetMapping
    public ResponseEntity<?> getAllBranches(@RequestParam(value = "search", required = false) String search) {
        log.info("[ADMIN] Fetching all branches & users with search: {}", search);
        List<User> allUsers;
        if (search != null && !search.trim().isEmpty()) {
            allUsers = userRepository.searchHierarchical(search.trim());
        } else {
            allUsers = userRepository.findAll();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : allUsers) {
            // Filter only shop owners, branch, or staff
            if (u.getRole() == null || "ROLE_ADMIN".equalsIgnoreCase(u.getRole().getName())) {
                continue;
            }
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId().toString());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("fullName", u.getFullName());
            map.put("phone", u.getPhone());
            map.put("address", u.getAddress());
            map.put("role", u.getRole().getName());
            map.put("parentId", u.getParentId());
            
            String parentName = "—";
            if (u.getParentId() != null && !u.getParentId().isBlank()) {
                parentName = userRepository.findById(new UserId(u.getParentId()))
                        .map(parent -> parent.getUsername() != null ? parent.getUsername() : parent.getEmail())
                        .orElse("—");
            }
            map.put("parentName", parentName);
            map.put("status", "ACTIVE"); // default active since no is_deleted field
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // POST /api/admin/branches
    @PostMapping
    public ResponseEntity<?> createBranch(@RequestBody Map<String, String> body) {
        log.info("[ADMIN] Request to create user/branch: {}", body);
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String roleName = body.get("role"); // e.g. "ROLE_SHOP_OWNER", "ROLE_BRANCH"
        String parentId = body.get("parentId");
        String fullName = body.get("fullName");
        String phone = body.get("phone");
        String address = body.get("address");

        if (username == null || username.isBlank() ||
            email == null || email.isBlank() ||
            password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập đầy đủ Username, Email và Mật khẩu!"));
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username đã tồn tại!"));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại!"));
        }

        if (roleName == null || roleName.isBlank()) {
            roleName = "ROLE_BRANCH";
        }
        
        final String targetRoleName = roleName;
        Role role = roleRepository.findByName(targetRoleName)
                .or(() -> roleRepository.findByName(targetRoleName.replace("ROLE_", "")))
                .or(() -> roleRepository.findByName("ROLE_" + targetRoleName))
                .orElseGet(() -> roleRepository.save(new Role(targetRoleName)));

        String hashedPassword = passwordEncoder.encode(password);
        User u = new User(UserId.random(), username, email, hashedPassword, role);
        u.setParentId(parentId);
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setAddress(address);

        userRepository.saveAndFlush(u);
        return ResponseEntity.ok(Map.of("message", "Khởi tạo thành công!", "id", u.getId().toString()));
    }

    // PUT /api/admin/branches/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBranch(@PathVariable String id, @RequestBody Map<String, String> body) {
        log.info("[ADMIN] Request to update user/branch: id={}, body={}", id, body);
        User u = userRepository.findById(new UserId(id))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi nhánh/người dùng"));

        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String roleName = body.get("role");
        String parentId = body.get("parentId");
        String fullName = body.get("fullName");
        String phone = body.get("phone");
        String address = body.get("address");

        if (username != null && !username.isBlank()) {
            Optional<User> existing = userRepository.findByUsername(username);
            if (existing.isPresent() && !existing.get().getId().toString().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username đã tồn tại!"));
            }
            u.setUsername(username);
        }

        if (email != null && !email.isBlank()) {
            Optional<User> existing = userRepository.findByEmail(email);
            if (existing.isPresent() && !existing.get().getId().toString().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại!"));
            }
            u.setEmail(email);
        }

        if (password != null && !password.isBlank() && !password.contains("•") && !password.contains("*")) {
            u.setPassword(passwordEncoder.encode(password));
        }

        if (roleName != null && !roleName.isBlank()) {
            final String targetRoleName = roleName;
            Role role = roleRepository.findByName(targetRoleName)
                    .or(() -> roleRepository.findByName(targetRoleName.replace("ROLE_", "")))
                    .or(() -> roleRepository.findByName("ROLE_" + targetRoleName))
                    .orElseGet(() -> roleRepository.save(new Role(targetRoleName)));
            u.setRole(role);
        }

        u.setParentId(parentId);
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setAddress(address);

        userRepository.saveAndFlush(u);
        return ResponseEntity.ok(Map.of("message", "Cập nhật thành công!"));
    }

    // DELETE /api/admin/branches/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable String id) {
        log.info("[ADMIN] Deleting branch/user: {}", id);
        userRepository.deleteById(new UserId(id));
        return ResponseEntity.ok(Map.of("message", "Đã xóa chi nhánh khỏi hệ thống thành công!"));
    }
}
