package ecommerce.example.ecommerce.application.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class Chat {
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
}