package ecommerce.example.ecommerce.infrastructure.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

/**
 * MinIO Service - File storage adapter for S3-compatible storage
 */
@Service
public class MinioService {

    @Value("${minio.url:http://localhost:9000}")
    private String minioUrl;

    @Value("${minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${minio.secret-key:minioadmin}")
    private String secretKey;

    private MinioClient minioClient;

    public MinioService() {
        // Empty constructor - initialization happens in PostConstruct
    }

    @PostConstruct
    private void initializeMinioClient() {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MinIO client", e);
        }
    }

    public MinioClient getClient() {
        return minioClient;
    }

    public String getUrl() {
        return minioUrl;
    }
}
