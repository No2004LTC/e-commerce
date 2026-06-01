package ecommerce.example.ecommerce.adapter.persistence.chat;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    /** Email / UUID của người gửi */
    private String senderId;

    /** Email / UUID của người nhận (hoặc "CHATBOT", "ADMIN") */
    private String recipientId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // ── Metadata fields enriched at save-time ─────────────────────────

    /** Tên hiển thị của người gửi (fullName hoặc username) */
    private String senderName;

    /** Role của người gửi: ROLE_ADMIN, ROLE_BRANCH, ROLE_STAFF, CHATBOT */
    private String senderRole;

    /**
     * Nhãn chi nhánh: tên chi nhánh của người gửi hoặc người nhận,
     * dùng để phân biệt chi nhánh chính / chi nhánh phụ trong giao diện Admin.
     */
    private String branchLabel;
}