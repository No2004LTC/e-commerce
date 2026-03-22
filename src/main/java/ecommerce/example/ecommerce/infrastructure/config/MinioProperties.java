package ecommerce.example.ecommerce.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioProperties {
    private String url;
    private String accessKey;
    private String secretKey;
    private String bucket;
}