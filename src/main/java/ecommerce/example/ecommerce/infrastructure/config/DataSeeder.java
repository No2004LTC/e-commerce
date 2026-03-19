package ecommerce.example.ecommerce.infrastructure.config;

import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.adapter.persistence.User.UserJpaRepository;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

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
        // 1. Khởi tạo Quyền (Roles) - Giữ nguyên vì Role dùng Long/Identity
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        // 2. Khởi tạo Tài khoản Admin
        if (userJpaRepository.findByUsername("admin").isEmpty()) {
            String hashedPassword = passwordEncoder.encode("admin");
            
            // QUAN TRỌNG: Bạn phải tạo UserId trước khi khởi tạo User
            UserId adminId = UserId.random(); 

            // Sử dụng Constructor 5 tham số (bao gồm cả UserId)
            User adminUser = new User(
                adminId, 
                "admin", 
                "admin@example.com", 
                hashedPassword, 
                adminRole
            );

            // Không cần ép kiểu rườm rà nếu UserJpaRepository đã kế thừa JpaRepository<User, UserId>
           ((JpaRepository<User, UserId>) userJpaRepository).save(adminUser);
            
            System.out.println("✅ [DataSeeder] Tài khoản admin với UUID đã được tạo.");
        }
    }
}