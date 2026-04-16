package ecommerce.example.ecommerce.adapter.persistence.chat;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;
    private String recipientId;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;
}