package ecommerce.example.ecommerce.adapter.web.User;

import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.application.dto.Profile;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller quản lý Chi nhánh (Branch) trong mô hình Multi-tenant.
 *
 * Luồng hoạt động:
 *  - Cửa hàng lớn (root) gọi GET /api/branches để lấy danh sách chi nhánh của mình.
 *  - POST /api/branches/link — Gán một tài khoản chi nhánh vào quyền quản lý của mình.
 *  - DELETE /api/branches/{branchId} — Tách chi nhánh ra khỏi chuỗi.
 */
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Slf4j
public class BranchController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    // =========================================================================
    // GET /api/branches — Lấy danh sách chi nhánh con của cửa hàng đang đăng nhập
    // =========================================================================
    @GetMapping
    public ResponseEntity<?> getBranches(Authentication auth) {
        User currentUser = userRepository.findByEmail(auth.getName())
            .or(() -> userRepository.findByUsername(auth.getName()))
            .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại: " + auth.getName()));

        if (currentUser.getRole() != null && "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole().getName())) {
            // Admin: Trả về danh sách toàn bộ các Chi nhánh lớn độc lập (ROLE_SHOP_OWNER) và danh sách con của từng chuỗi
            List<User> shopOwners = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ROLE_SHOP_OWNER".equalsIgnoreCase(u.getRole().getName()))
                .collect(Collectors.toList());

            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (User owner : shopOwners) {
                List<User> children = userRepository.findByParentId(owner.getId().toString());
                List<Map<String, Object>> childMaps = children.stream()
                    .map(c -> {
                        Map<String, Object> m = new java.util.HashMap<>();
                        m.put("id", c.getId().toString());
                        m.put("username", c.getUsername());
                        m.put("email", c.getEmail());
                        m.put("role", c.getRole() != null ? c.getRole().getName() : "ROLE_BRANCH");
                        m.put("parentId", owner.getId().toString());
                        return m;
                    })
                    .collect(Collectors.toList());

                Map<String, Object> ownerMap = new java.util.HashMap<>();
                ownerMap.put("id", owner.getId().toString());
                ownerMap.put("username", owner.getUsername());
                ownerMap.put("email", owner.getEmail());
                ownerMap.put("role", owner.getRole() != null ? owner.getRole().getName() : "ROLE_SHOP_OWNER");
                ownerMap.put("branches", childMaps);
                result.add(ownerMap);
            }
            return ResponseEntity.ok(result);
        } else {
            // Shop Owner hoặc Chi nhánh con
            String currentUserId = currentUser.getId().toString();
            if (currentUser.getParentId() != null && !currentUser.getParentId().isBlank()) {
                currentUserId = currentUser.getParentId();
            }
            List<User> branches = userRepository.findByParentId(currentUserId);
            List<Map<String, Object>> result = branches.stream()
                .map(u -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", u.getId().toString());
                    m.put("username", u.getUsername());
                    m.put("email", u.getEmail());
                    m.put("parentId", u.getParentId());
                    m.put("role", u.getRole() != null ? u.getRole().getName() : "ROLE_BRANCH");
                    return m;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        }
    }

    // =========================================================================
    // POST /api/branches/link — Gán chi nhánh vào chuỗi của cửa hàng lớn
    // Body: { "branchUsername": "branch_chi_nhanh_1" }
    // =========================================================================
    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> linkBranch(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        String currentUserId = resolveUserId(auth);
        String branchUsername = body.get("branchUsername");

        if (branchUsername == null || branchUsername.isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Vui lòng cung cấp branchUsername!"));
        }

        User branch = userRepository.findByUsername(branchUsername)
            .or(() -> userRepository.findByEmail(branchUsername))
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản chi nhánh: " + branchUsername));

        if (!branch.isRootShop()) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "Tài khoản này đã thuộc một chuỗi khác!"));
        }

        branch.setParentId(currentUserId);
        userRepository.persist(branch);

        return ResponseEntity.ok(Map.of(
            "message",  "Đã liên kết chi nhánh thành công!",
            "branchId", branch.getId().toString()
        ));
    }

    // =========================================================================
    // DELETE /api/branches/{branchId} — Tách chi nhánh ra khỏi chuỗi
    // =========================================================================
    @DeleteMapping("/{branchId}")
    public ResponseEntity<Map<String, String>> unlinkBranch(
            @PathVariable String branchId,
            Authentication auth) {

        String currentUserId = resolveUserId(auth);

        User branch = userRepository.findByParentId(currentUserId).stream()
            .filter(u -> u.getId().toString().equals(branchId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException(
                "Chi nhánh " + branchId + " không thuộc quyền quản lý của bạn!"));

        branch.setParentId(null);
        userRepository.persist(branch);

        return ResponseEntity.ok(Map.of("message", "Đã tách chi nhánh thành công!"));
    }

    // =========================================================================
    // POST /api/branches — Tạo mới và liên kết chi nhánh tự động
    // =========================================================================
    @PostMapping
    public ResponseEntity<?> createBranch(
            @RequestBody Map<String, String> body,
            Authentication auth) {
        log.info("[BRANCH] Request to create sub-branch: {}", body);
        String currentUserId = resolveUserId(auth);
        
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        
        if (username == null || username.isBlank() ||
            email == null || email.isBlank() ||
            password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập đầy đủ Username, Email và Mật khẩu!"));
        }
        
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username đã tồn tại trên hệ thống!"));
        }
        
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã tồn tại trên hệ thống!"));
        }
        
        // Gán Role ROLE_BRANCH cho tài khoản con này để phân cấp Multi-tenant thông suốt
        ecommerce.example.ecommerce.domain.user.Role userRole = roleRepository.findByName("ROLE_BRANCH")
                .or(() -> roleRepository.findByName("BRANCH"))
                .or(() -> roleRepository.findByName("ROLE_BRANCH_USER"))
                .or(() -> roleRepository.findByName("BRANCH_USER"))
                .orElseGet(() -> {
                    return roleRepository.save(new ecommerce.example.ecommerce.domain.user.Role("ROLE_BRANCH"));
                });
        
        // Hash password sử dụng Argon2id (PasswordEncoder được cấu hình Argon2 trong SecurityConfig)
        String hashedPassword = passwordEncoder.encode(password);
        
        // Tự động sinh chuỗi UUID khóa chính
        ecommerce.example.ecommerce.domain.user.User branch = new ecommerce.example.ecommerce.domain.user.User(
            ecommerce.example.ecommerce.domain.user.UserId.random(),
            username,
            email,
            hashedPassword,
            userRole
        );
        branch.setParentId(currentUserId);
        
        userRepository.persist(branch);
        
        return ResponseEntity.ok(Map.of(
            "message", "Tạo và liên kết chi nhánh thành công!",
            "id", branch.getId().toString(),
            "username", branch.getUsername()
        ));
    }

    // ── Helper: lấy userId từ JWT Authentication ──────────────────────────────
    private String resolveUserId(Authentication auth) {
        // auth.getName() trả về email hoặc username từ JWT; hỗ trợ tìm kiếm cả hai
        User user = userRepository.findByEmail(auth.getName())
            .or(() -> userRepository.findByUsername(auth.getName()))
            .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        return user.getId().toString();
    }
}
