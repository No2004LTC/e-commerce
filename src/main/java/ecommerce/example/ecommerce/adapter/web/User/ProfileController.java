package ecommerce.example.ecommerce.adapter.web.User; // Đổi package từ Auth sang User

import ecommerce.example.ecommerce.application.User.GetProfileUseCase;
import ecommerce.example.ecommerce.application.User.UpdateProfileUseCase;
import ecommerce.example.ecommerce.application.dto.Profile;
import ecommerce.example.ecommerce.domain.user.UserId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    public ProfileController(GetProfileUseCase getProfileUseCase, UpdateProfileUseCase updateProfileUseCase) {
        this.getProfileUseCase = getProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> get(@PathVariable UUID id) {
        return ResponseEntity.ok(getProfileUseCase.execute(new UserId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profile> update(
            @PathVariable UUID id, 
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(updateProfileUseCase.execute(
            new UserId(id), 
            body.get("email"), 
            body.get("avatarUrl")
        ));
    }
}