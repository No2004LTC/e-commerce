package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.application.dto.Profile;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class UpdateProfileUseCase {
    private final UserService userService;

    public UpdateProfileUseCase(UserService userService) { 
        this.userService = userService; 
    }

    public Profile execute(UserId id, String newEmail, String newAvatarUrl) {
        User user = userService.findById(id)
                .orElseThrow(() -> new UseCaseException("User not found"));
        
        user.setEmail(newEmail);
        user.setAvatarUrl(newAvatarUrl);
        
        return Profile.fromDomain(userService.save(user));
    }
}