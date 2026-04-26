package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.adapter.security.JwtTokenProvider;
import ecommerce.example.ecommerce.adapter.web.Auth.AuthResponse;
import ecommerce.example.ecommerce.adapter.web.Auth.LoginRequest;
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
        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UseCaseException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UseCaseException("Invalid username or password");
        }

        
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId().toString());

        return new AuthResponse(user.getId().toString(), token, user.getUsername(), user.getRole().getName());
    }
}

