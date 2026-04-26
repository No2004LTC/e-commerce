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
        
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        
        if (userJpaRepository.findByUsername("admin").isEmpty()) {
            String hashedPassword = passwordEncoder.encode("admin");
            
            
            UserId adminId = UserId.random(); 

            
            User adminUser = new User(
                adminId, 
                "admin", 
                "admin@example.com", 
                hashedPassword, 
                adminRole
            );

           
           ((JpaRepository<User, UserId>) userJpaRepository).save(adminUser);
            
            System.out.println("✅ [DataSeeder] Tài khoản admin với UUID đã được tạo.");
        }
    }
}