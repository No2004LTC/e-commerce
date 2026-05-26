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

        // 2. LOGIC KIỂM TRA BÀNG USER RỖNG HOẶC TÀI KHOẢN ADMIN CHƯA TỒN TẠI (IDEMPOTENT INITIALIZATION)
        boolean adminExists = userJpaRepository.findByUsername("admin").isPresent() 
                || userJpaRepository.findByEmail("admin@gmail.com").isPresent();
        if (!adminExists) {
            log.info("Admin account (admin/admin@gmail.com) not found. Initializing default supreme Admin account...");

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
        } else {
            log.info("Supreme Admin account already exists. Skipping default Admin initialization.");
        }
    }
}