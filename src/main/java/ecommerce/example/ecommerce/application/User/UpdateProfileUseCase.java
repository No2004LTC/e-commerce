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
@RequiredArgsConstructor // Tự động tạo constructor cho UserService và PasswordEncoder
public class UpdateProfileUseCase {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional // Đảm bảo tính toàn vẹn dữ liệu khi update
    public Profile execute(UserId id, String newUsername, String newEmail, String newPassword, String newAvatarUrl) {
        User user = userService.findById(id)
                .orElseThrow(() -> new UseCaseException("User not found " ));
        
        // Chỉ cập nhật nếu giá trị truyền vào không null hoặc không trống
        if (newUsername != null && !newUsername.isBlank()) {
            user.setUsername(newUsername);
        }
        
        if (newEmail != null && !newEmail.isBlank()) {
            user.setEmail(newEmail);
        }

        if (newAvatarUrl != null) {
            user.setAvatarUrl(newAvatarUrl);
        }

        // Xử lý đổi mật khẩu: Mã hóa trước khi lưu
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        
        // Lưu và chuyển đổi sang DTO để trả về
        User savedUser = userService.save(user);
        return Profile.fromDomain(savedUser);
    }
}