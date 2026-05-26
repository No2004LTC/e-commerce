package ecommerce.example.ecommerce.adapter.web.chat;

import ecommerce.example.ecommerce.application.chat.ChatService;
import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho lịch sử Chat.
 *
 * GET /api/chat/history/{partnerId} — Lấy lịch sử hội thoại giữa user hiện tại và một user khác
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Lấy lịch sử hội thoại giữa người dùng đang đăng nhập và một người khác.
     *
     * @param partnerId Username / userId của người kia
     * @param auth      Authentication từ JWT token
     * @return Danh sách tin nhắn sắp xếp theo thời gian tăng dần
     */
    @GetMapping("/history/{partnerId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String partnerId,
            Authentication auth) {
        String currentUserId = auth.getName();
        List<ChatMessage> history = chatService.getHistory(currentUserId, partnerId);
        return ResponseEntity.ok(history);
    }
}
