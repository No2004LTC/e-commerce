package ecommerce.example.ecommerce.domain.chat;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    /** DB primary key — used by frontend for deduplication */
    private String id;

    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
    private String type;

    // ── Rich metadata fields ─────────────────────────────────────────

    /** Tên hiển thị của người gửi (fullName / username) */
    private String senderName;

    /** Role của người gửi: ROLE_ADMIN, ROLE_BRANCH, ROLE_STAFF, CHATBOT */
    private String senderRole;

    /**
     * Nhãn chi nhánh, ví dụ:
     *   "minh (Chi nhánh chính)"
     *   "long / minh"  ← chi nhánh phụ long thuộc chain minh
     */
    private String branchLabel;
}