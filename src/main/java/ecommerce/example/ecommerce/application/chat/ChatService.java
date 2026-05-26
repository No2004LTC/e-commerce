package ecommerce.example.ecommerce.application.chat;

import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import ecommerce.example.ecommerce.domain.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Application Service cho luồng Chat.
 *
 * Quy tắc timestamp: Server LUÔN override timestamp bằng LocalDateTime.now()
 * để tránh client gửi timestamp giả mạo hoặc lệch múi giờ.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Xử lý và lưu một tin nhắn chat.
     * Timestamp vật lý được ghi từ Server tại thời điểm nhận — không lấy từ client.
     *
     * @param message Tin nhắn thô từ WebSocket
     * @return ChatMessage đã được gán timestamp server và lưu vào DB
     */
    public ChatMessage processAndSave(ChatMessage message) {
        // ✅ Luôn ghi timestamp từ Server — đảm bảo tính chính xác vật lý
        message.setTimestamp(LocalDateTime.now().format(FORMATTER));

        chatRepository.save(message);
        return message;
    }

    /**
     * Lấy lịch sử hội thoại giữa 2 người dùng (bidirectional).
     */
    public List<ChatMessage> getHistory(String user1, String user2) {
        return chatRepository.getHistory(user1, user2);
    }
}