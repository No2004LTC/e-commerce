package ecommerce.example.ecommerce.domain.chat;

import lombok.*;

@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor
@Builder
public class ChatMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
}