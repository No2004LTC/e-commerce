package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.application.dto.Profile;
import ecommerce.example.ecommerce.domain.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class GetProfileUseCase {
    private final UserService userService;

    public GetProfileUseCase(UserService userService) { 
        this.userService = userService; 
    }

    public Profile execute(UserId id) {
        return userService.findById(id)
                .map(Profile::fromDomain)
                .orElseThrow(() -> new UseCaseException("User not found"));
    }
}