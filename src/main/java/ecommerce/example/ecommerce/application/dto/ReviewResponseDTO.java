package ecommerce.example.ecommerce.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {
    private String id;
    private String productId;
    private String userId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}