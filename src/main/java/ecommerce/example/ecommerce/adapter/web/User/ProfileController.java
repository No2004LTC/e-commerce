package ecommerce.example.ecommerce.adapter.web.User;

import ecommerce.example.ecommerce.application.User.UpdateProfileUseCase;
import ecommerce.example.ecommerce.application.User.UserService;
import ecommerce.example.ecommerce.application.dto.Profile;
import ecommerce.example.ecommerce.domain.user.UserId;
import ecommerce.example.ecommerce.application.User.UploadAvatarUseCase;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


record UpdateProfileRequest(String username, String email, String avatarUrl, String password) {}

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UpdateProfileUseCase updateProfileUseCase;
    private final UserService userService;
    private final UploadAvatarUseCase uploadAvatarUseCase;
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
                
                request.password(), 
                 request.avatarUrl()
        );
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        userService.deleteById(new UserId(UUID.fromString(id)));
        return ResponseEntity.noContent().build();
    }
  @PostMapping(value = "/avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<String> uploadAvatar(
        @RequestParam("file") MultipartFile file, 
        Authentication authentication) {
    
   
    String currentUserId = authentication.getName(); 
    
    System.out.println("DEBUG: Current User ID from Token: " + currentUserId);
    String avatarUrl = uploadAvatarUseCase.execute(currentUserId, file);
    return ResponseEntity.ok(avatarUrl);
}
}