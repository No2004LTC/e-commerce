package ecommerce.example.ecommerce.infrastructure.config;

import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.adapter.persistence.User.UserJpaRepository;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository; // Cần thiết để ép kiểu

@Component
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
        // 1. Khởi tạo Quyền (Roles)
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        // 2. Khởi tạo Tài khoản Admin
        if (userJpaRepository.findByUsername("admin").isEmpty()) {
            String hashedPassword = passwordEncoder.encode("admin");
            
            // Sử dụng Constructor 4 tham số mà bạn đã định nghĩa trong file User.java
            User adminUser = new User("admin", "admin@example.com", hashedPassword, adminRole);

            // GIẢI PHÁP TRIỆT ĐỂ: Ép kiểu về JpaRepository để Java không bị nhầm lẫn phương thức save
            ((JpaRepository<User, Long>) userJpaRepository).save(adminUser);
            
            System.out.println("✅ [DataSeeder] Tài khoản admin/admin đã được tạo thành công.");
        }
    }
}