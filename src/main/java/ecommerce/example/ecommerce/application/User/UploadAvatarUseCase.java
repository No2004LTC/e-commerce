package ecommerce.example.ecommerce.application.User;

import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.domain.user.UserId;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import ecommerce.example.ecommerce.infrastructure.minio.MinioStorageService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadAvatarUseCase {

    private final UserRepository userRepository;
    private final MinioStorageService storageService;

    // Sử dụng Constructor truyền thống để tránh lỗi Lombok "Cannot resolve symbol"
    public UploadAvatarUseCase(UserRepository userRepository, MinioStorageService storageService) {
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional
    public String execute(String userIdStr, MultipartFile file) {
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new RuntimeException("User not authenticated");
        }

        try {
            // Chuyển đổi String ID từ Token thành VO UserId
            UserId userId = UserId.fromString(userIdStr);

            // Tìm User trong Database
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userIdStr));

            // Upload ảnh lên MinIO và nhận lại đường dẫn (bucket/folder/filename)
            String avatarUrl = storageService.uploadFile(file, "avatars");

            // Cập nhật thông tin User
            user.setAvatarUrl(avatarUrl);
            userRepository.persist(user);

            return avatarUrl;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid UUID format: " + userIdStr);
        }
    }
}