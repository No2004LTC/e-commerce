package ecommerce.example.ecommerce.adapter.web.Auth;

import ecommerce.example.ecommerce.application.User.RegisterUserUseCase;
import ecommerce.example.ecommerce.application.User.LoginUserUseCase;
import ecommerce.example.ecommerce.application.User.UploadAvatarUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final UploadAvatarUseCase uploadAvatarUseCase;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(registerUserUseCase.execute(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUserUseCase.execute(request));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal String userId,
            @RequestParam("file") MultipartFile file) {

        String url = uploadAvatarUseCase.execute(userId, file);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }
}