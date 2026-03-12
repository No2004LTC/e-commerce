package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.application.User.UserService;
import ecommerce.example.ecommerce.application.common.UseCaseException;
import ecommerce.example.ecommerce.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class RegisterUserUseCase {

    private final UserService userService;

    public RegisterUserUseCase(UserService userService) {
        this.userService = userService;
    }

    public User execute(User user) {
        if (user.getUsername() == null || user.getEmail() == null) {
            throw new UseCaseException("Username and email must be provided");
        }
        return userService.register(user);
    }
}
