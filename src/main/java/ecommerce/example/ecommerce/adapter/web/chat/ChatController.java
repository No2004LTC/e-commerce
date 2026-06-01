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
 * GET /api/chat/history/{partnerId}    — Lịch sử hội thoại với một user cụ thể (JWT auth)
 * GET /api/chat/history?user1=&user2=  — Lịch sử hội thoại giữa 2 user (admin/query)
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Lấy lịch sử chat của người đăng nhập với một đối tác.
     */
    @GetMapping("/history/{partnerId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable String partnerId,
            Authentication auth) {
        String currentUserId = auth.getName();
        return ResponseEntity.ok(chatService.getHistory(currentUserId, partnerId));
    }

    /**
     * Lấy lịch sử chat giữa 2 user bất kỳ (dành cho Admin panel).
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getChatHistoryByQuery(
            @RequestParam("user1") String user1,
            @RequestParam("user2") String user2) {
        return ResponseEntity.ok(chatService.getHistory(user1, user2));
    }
}
