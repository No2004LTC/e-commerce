package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.adapter.persistence.RoleRepository;
import ecommerce.example.ecommerce.adapter.security.JwtTokenProvider;
import ecommerce.example.ecommerce.adapter.web.Auth.AuthResponse;
import ecommerce.example.ecommerce.adapter.web.Auth.RegisterRequest;
import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.domain.user.Role;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
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
        // 1. Validation cơ bản
        if (request.getUsername() == null || request.getEmail() == null || request.getPassword() == null) {
            throw new UseCaseException("Username, email, and password must be provided");
        }

        // 2. Kiểm tra trùng lặp qua UserService
        if (userService.findByUsername(request.getUsername()).isPresent()) {
            throw new UseCaseException("Username already exists");
        }

        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new UseCaseException("Email already exists");
        }

        // 3. Lấy Role mặc định
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new UseCaseException("Default role not found"));

        // 4. Mã hóa mật khẩu (Argon2/BCrypt tùy config của bạn)
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 5. Tạo Entity User mới với UUID ngẫu nhiên
        User user = new User(UserId.random(), request.getUsername(), request.getEmail(), hashedPassword, userRole);

        // 6. Lưu vào Database
        User savedUser = userService.register(user);

        // 7. CẬP NHẬT: Tạo JWT với 2 tham số (username và userId)
        String token = jwtTokenProvider.generateToken(
                savedUser.getUsername(),
                savedUser.getId().toString()
        );

        // 8. Trả về Response cho Controller
        return new AuthResponse(
                savedUser.getId().toString(),
                token,
                savedUser.getUsername(),
                savedUser.getRole().getName()
        );
    }
}