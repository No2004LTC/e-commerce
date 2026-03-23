package ecommerce.example.ecommerce.adapter.web.User;

import ecommerce.example.ecommerce.application.User.UpdateProfileUseCase;
import ecommerce.example.ecommerce.application.User.UserService;
import ecommerce.example.ecommerce.application.dto.Profile;
import ecommerce.example.ecommerce.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// Thêm trường password vào DTO
record UpdateProfileRequest(String username, String email, String avatarUrl, String password) {}

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UpdateProfileUseCase updateProfileUseCase;
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getProfile(@PathVariable String id) {
        return userService.findById(new UserId(UUID.fromString(id)))
                .map(Profile::fromDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profile> updateProfile(
            @PathVariable String id, 
            @RequestBody UpdateProfileRequest request) { 
        
        Profile updated = updateProfileUseCase.execute(
                new UserId(UUID.fromString(id)), 
                request.username(),
                request.email(), 
                request.avatarUrl(),
                request.password() // Truyền password vào đây
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        userService.deleteById(new UserId(UUID.fromString(id)));
        return ResponseEntity.noContent().build();
    }
}