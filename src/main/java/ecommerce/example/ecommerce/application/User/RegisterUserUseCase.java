package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.adapter.in.web.AuthResponse;
import ecommerce.example.ecommerce.adapter.in.web.RegisterRequest;
import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.adapter.security.JwtTokenProvider;
import ecommerce.example.ecommerce.application.User.UserService;
import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserUseCase {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RegisterUserUseCase(UserService userService, PasswordEncoder passwordEncoder, RoleRepository roleRepository, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse execute(RegisterRequest request) {
        if (request.getUsername() == null || request.getEmail() == null || request.getPassword() == null) {
            throw new UseCaseException("Username, email, and password must be provided");
        }

        // Check if username already exists
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            throw new UseCaseException("Username already exists");
        }

        // Check if email already exists
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new UseCaseException("Email already exists");
        }

        // Find default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new UseCaseException("Default role not found"));

        // Hash password
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // Create user
        User user = new User(request.getUsername(), request.getEmail(), hashedPassword, userRole);

        // Save user
        User savedUser = userService.register(user);

        // Generate JWT
        String token = jwtTokenProvider.generateToken(savedUser.getUsername());

        return new AuthResponse(token, savedUser.getUsername(), savedUser.getRole().getName());
    }
}
