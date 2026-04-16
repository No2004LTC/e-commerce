package ecommerce.example.ecommerce.adapter.web.chat;

import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * API 1: Xử lý chat 1-1 thông thường
     */
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now().toString());

        log.info("[CHAT] Tin nhắn từ {} đến {}", chatMessage.getSenderId(), chatMessage.getRecipientId());

        messagingTemplate.convertAndSendToUser(
                chatMessage.getRecipientId(), 
                "/queue/messages", 
                chatMessage
        );
    }

    /**
     * API 2: Tự động chào khi User bắt đầu nhấn vào Chat
     * Logic: ShopId = SenderId + "shop"
     */
    @MessageMapping("/start-chat")
    public void startChat(@Payload ChatMessage chatMessage, Principal principal) {
        // Lấy tên user từ Token (Nếu đã cấu hình Security) hoặc từ JSON gửi lên
        String username = (principal != null) ? principal.getName() : chatMessage.getSenderId();
        
        // 1. Gán ID của Shop theo công thức: username + "shop"
        String dynamicShopId = username + "shop";
        
        // 2. Tạo nội dung chào (Context) theo tên shop mới
        String welcomeContent = "Xin chào, đây là " + dynamicShopId + "! Tôi có thể giúp gì cho bạn?";

        ChatMessage welcomeMsg = ChatMessage.builder()
                .senderId(dynamicShopId)          // ID người gửi là Shop ảo
                .recipientId(username)             // Gửi ngược lại cho chính User đó
                .content(welcomeContent)
                .timestamp(LocalDateTime.now().toString())
                .build();

        log.info("[WELCOME] Shop {} đang chào khách hàng {}", dynamicShopId, username);

        // Bắn tin nhắn về đường dẫn cá nhân của User
        messagingTemplate.convertAndSendToUser(
                username, 
                "/queue/messages", 
                welcomeMsg
        );
    }
}