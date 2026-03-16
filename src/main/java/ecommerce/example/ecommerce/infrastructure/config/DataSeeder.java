package ecommerce.example.ecommerce.infrastructure.config;

import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.adapter.persistence.User.UserJpaRepository;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, UserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default roles
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        // Seed admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            String hashedPassword = passwordEncoder.encode("admin");
            User adminUser = new User("admin", "admin@example.com", hashedPassword, adminRole);
            ((UserRepository) userRepository).save(adminUser);
        }
    }
}