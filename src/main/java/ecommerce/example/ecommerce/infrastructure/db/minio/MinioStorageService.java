package ecommerce.example.ecommerce.infrastructure.db.minio;

import ecommerce.example.ecommerce.infrastructure.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs; // Thêm import này
import jakarta.annotation.PostConstruct; // Thêm import này
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    // Tự động chạy khi Server BE khởi động để mở quyền Public cho Bucket
    @PostConstruct
    public void initBucketPolicy() {
        try {
            String bucketName = minioProperties.getBucket();
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Chuỗi JSON Policy cho phép đọc ảnh công khai không cần Token
            String publicReadPolicy = "{\n" +
                    "  \"Version\": \"2012-10-17\",\n" +
                    "  \"Statement\": [\n" +
                    "    {\n" +
                    "      \"Effect\": \"Allow\",\n" +
                    "      \"Principal\": \"*\",\n" +
                    "      \"Action\": [\"s3:GetObject\"],\n" +
                    "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder().bucket(bucketName).config(publicReadPolicy).build()
            );
            log.info("MinIO Bucket '{}' đã được cấu hình PUBLIC thành công!", bucketName);
        } catch (Exception e) {
            log.error("Lỗi cấu hình Public Policy cho MinIO: {}", e.getMessage());
        }
    }

    public String uploadFile(MultipartFile file, String folder) {
        try {
            log.info("Connecting to MinIO at: {}", minioProperties.getUrl());

            String fileName = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // SỬA Ở ĐÂY: Trả về URL đầy đủ bao gồm cả http://localhost:9000 để Frontend hiển thị được ảnh luôn
            return String.format("%s/%s/%s", minioProperties.getUrl(), minioProperties.getBucket(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Chi tiết lỗi MinIO: {}", e.getMessage());
            throw new RuntimeException("MinIO Error [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
    }
}