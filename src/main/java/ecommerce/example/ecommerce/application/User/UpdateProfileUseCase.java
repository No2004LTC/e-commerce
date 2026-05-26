package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.application.dto.Profile;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor 
public class UpdateProfileUseCase {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional 
    public Profile execute(
            UserId id, 
            String newUsername, 
            String newEmail, 
            String newPassword, 
            String newAvatarUrl,
            String newFullName,
            String newPhone,
            String newAddress) {
        
        User user = userService.findById(id)
                .orElseThrow(() -> new UseCaseException("User not found"));
        
        if (newUsername != null && !newUsername.isBlank()) {
            user.setUsername(newUsername);
        }
        
        if (newEmail != null && !newEmail.isBlank()) {
            user.setEmail(newEmail);
        }

        if (newAvatarUrl != null) {
            user.setAvatarUrl(newAvatarUrl);
        }

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (newFullName != null) {
            user.setFullName(newFullName);
        }

        if (newPhone != null) {
            user.setPhone(newPhone);
        }

        if (newAddress != null) {
            user.setAddress(newAddress);
        }
        
        User savedUser = userService.save(user);
        return Profile.fromDomain(savedUser);
    }
}