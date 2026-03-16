package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.adapter.in.web.AuthResponse;
import ecommerce.example.ecommerce.adapter.in.web.LoginRequest;
import ecommerce.example.ecommerce.adapter.security.JwtTokenProvider;
import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LoginUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginUserUseCase(UserRepository repository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse execute(LoginRequest request) {
        Optional<User> userOpt = repository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new UseCaseException("Invalid username or password");
        }

        User user = userOpt.get();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UseCaseException("Invalid username or password");
        }

        // Generate JWT
        String token = jwtTokenProvider.generateToken(user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getRole().getName());
    }
}
