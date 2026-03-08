package ecommerce.example.ecommerce.service;

import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.dto.LoginRequest;
import ecommerce.example.ecommerce.dto.RegisterRequest;
import ecommerce.example.ecommerce.repository.RoleRepository;
import ecommerce.example.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User register(RegisterRequest registerRequest) {
        // Check if user already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Get default role
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword()); // Should be encrypted in production
        user.setRole(role);

        return userRepository.save(user);
    }

    public Optional<User> login(LoginRequest loginRequest) {
        return userRepository.findByUsername(loginRequest.getUsername());
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
