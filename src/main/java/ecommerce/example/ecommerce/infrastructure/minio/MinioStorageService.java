package ecommerce.example.ecommerce.infrastructure.minio;

import ecommerce.example.ecommerce.infrastructure.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
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

    public String uploadFile(MultipartFile file, String folder) {
        try {
            // Log để kiểm tra URL đang dùng là gì
            log.info("Connecting to MinIO at: {}", minioProperties.getUrl());

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucket()).build());
            }

            String fileName = folder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return String.format("%s/%s", minioProperties.getBucket(), fileName);
        } catch (Exception e) {
            // QUAN TRỌNG: In toàn bộ StackTrace ra console để debug
            e.printStackTrace();
            log.error("Chi tiết lỗi MinIO: {}", e.getMessage());

            // Trả về thông báo lỗi cụ thể thay vì chung chung
            throw new RuntimeException("MinIO Error [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
        }
    }
}