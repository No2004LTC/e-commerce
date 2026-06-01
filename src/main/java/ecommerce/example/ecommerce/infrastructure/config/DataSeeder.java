package ecommerce.example.ecommerce.infrastructure.config;

import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.adapter.persistence.User.UserJpaRepository;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserJpaRepository userJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, UserJpaRepository userJpaRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userJpaRepository = userJpaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database seeding logic...");

        // 1. ĐẢM BẢO THỨ TỰ DEPENDENCY VÀ KHỞI TẠO QUYỀN (ROLES)
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .or(() -> roleRepository.findByName("ADMIN"))
                .orElseGet(() -> {
                    log.info("Role ROLE_ADMIN not found. Saving default ROLE_ADMIN...");
                    return roleRepository.save(new Role("ROLE_ADMIN"));
                });

        Role shopOwnerRole = roleRepository.findByName("ROLE_SHOP_OWNER")
                .or(() -> roleRepository.findByName("SHOP_OWNER"))
                .orElseGet(() -> {
                    log.info("Role ROLE_SHOP_OWNER not found. Saving default ROLE_SHOP_OWNER...");
                    return roleRepository.save(new Role("ROLE_SHOP_OWNER"));
                });

        Role branchRole = roleRepository.findByName("ROLE_BRANCH")
                .or(() -> roleRepository.findByName("BRANCH"))
                .orElseGet(() -> {
                    log.info("Role ROLE_BRANCH not found. Saving default ROLE_BRANCH...");
                    return roleRepository.save(new Role("ROLE_BRANCH"));
                });

        // Đảm bảo ROLE_USER cũng có mặt trong hệ thống
        roleRepository.findByName("ROLE_USER")
                .or(() -> roleRepository.findByName("USER"))
                .orElseGet(() -> {
                    log.info("Role ROLE_USER not found. Saving default ROLE_USER...");
                    return roleRepository.save(new Role("ROLE_USER"));
                });

        // 2. LOGIC KIỂM TRA BẢNG USER RỖNG HOẶC TÀI KHOẢN ADMIN CHƯA TỒN TẠI (IDEMPOTENT INITIALIZATION)
        var adminOpt = userJpaRepository.findByUsername("admin")
                .or(() -> userJpaRepository.findByEmail("admin@gmail.com"));

        if (adminOpt.isPresent()) {
            User existingAdmin = adminOpt.get();
            // Nếu mật khẩu trong DB chưa được băm Argon2id (không bắt đầu bằng prefix băm $argon2id$) hoặc là chuỗi thô "admin"
            if ("admin".equals(existingAdmin.getPassword()) || !existingAdmin.getPassword().startsWith("$argon2id$")) {
                log.info("Mật khẩu tài khoản Admin hiện hành chưa được băm Argon2id. Tiến hành băm và cập nhật...");
                existingAdmin.setPassword(passwordEncoder.encode("admin"));
                userJpaRepository.save(existingAdmin);
                log.info("Đã cập nhật mật khẩu băm Argon2id thành công cho tài khoản Admin!");
            } else {
                log.info("Tài khoản quản trị Admin đã tồn tại với mật khẩu được mã hóa an toàn.");
            }
        } else {
            log.info("Tài khoản Admin chưa tồn tại. Bắt đầu khởi tạo tài khoản quản trị tối cao...");

            // 3. THÔNG TIN KHỞI TẠO TÀI KHOẢN ADMIN TỐI CAO
            String adminUuid = UUID.randomUUID().toString();
            UserId adminId = new UserId(adminUuid);
            String hashedPassword = passwordEncoder.encode("admin");

            User adminUser = new User(
                    adminId,
                    "admin",
                    "admin@gmail.com",
                    hashedPassword,
                    adminRole
            );
            adminUser.setParentId(null);

            userJpaRepository.save(adminUser);

            log.info("INFO: Default Admin Account initialized successfully!");
            log.info("Default Admin Account Details - UUID: {}, Username: {}, Email: {}, Role: {}",
                    adminUuid, adminUser.getUsername(), adminUser.getEmail(), adminRole.getName());
        }
    }
}