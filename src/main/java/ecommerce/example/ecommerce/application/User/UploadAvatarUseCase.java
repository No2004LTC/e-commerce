package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import ecommerce.example.ecommerce.infrastructure.minio.MinioStorageService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadAvatarUseCase {

    private final UserRepository userRepository;
    private final MinioStorageService storageService;

    public UploadAvatarUseCase(UserRepository userRepository, MinioStorageService storageService) {
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional
    public String execute(String username, MultipartFile file) {
        // 1. Kiểm tra đầu vào
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("User not authenticated");
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            // 2. Tìm User trong Database bằng USERNAME (Thay vì dùng UserId.fromString)
            // Lưu ý: Đảm bảo UserRepository của bạn có hàm findByUsername
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

            // 3. Upload ảnh lên MinIO
            String avatarUrl = storageService.uploadFile(file, "avatars");

            // 4. Cập nhật thông tin User
            user.setAvatarUrl(avatarUrl);
            userRepository.persist(user);

            return avatarUrl;
        } catch (Exception e) {
            // Log lỗi chi tiết để debug
            System.err.println("Error during avatar upload: " + e.getMessage());
            throw new RuntimeException("Could not upload avatar: " + e.getMessage());
        }
    }
}