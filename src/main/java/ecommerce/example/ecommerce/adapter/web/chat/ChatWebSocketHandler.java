package ecommerce.example.ecommerce.adapter.web.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.example.ecommerce.application.chat.ChatService;
import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    // Lưu trữ các session đang kết nối: Map<SenderId, Session>
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // Đánh dấu những User đã được chào trong phiên làm việc này để tránh chào lặp đi lặp lại
    private static final Set<String> welcomedUsers = ConcurrentHashMap.newKeySet();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 1. Đọc dữ liệu JSON gửi lên
        String payload = message.getPayload();
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        
        String senderId = chatMessage.getSenderId();
        
        // 2. Đăng ký session nếu đây là lần đầu người này gửi tin trong kết nối này
        if (!sessions.containsKey(senderId)) {
            sessions.put(senderId, session);
            log.info("[SOCKET] Đã đăng ký định danh cho User: {}", senderId);
        }

        // 3. LOGIC LỜI CHÀO TỰ ĐỘNG
        // Nếu Frontend gửi content là "OPEN_CHAT" hoặc đây là tin nhắn đầu tiên của User này
        if ("OPEN_CHAT".equalsIgnoreCase(chatMessage.getContent()) || !welcomedUsers.contains(senderId)) {
            sendAutoWelcomeMessage(session, senderId);
            
            // Nếu khách chỉ ấn vào để mở (OPEN_CHAT) thì không cần xử lý lưu tin nhắn thô này
            if ("OPEN_CHAT".equalsIgnoreCase(chatMessage.getContent())) return;
        }

        // 4. XỬ LÝ TIN NHẮN CHAT BÌNH THƯỜNG
        handlePrivateMessage(chatMessage);
    }

    private void sendAutoWelcomeMessage(WebSocketSession session, String customerId) throws Exception {
        String shopId = customerId + "shop";
        String welcomeContent = "Xin chào, đây là " + shopId + "! có thể giúp gì cho bạn?";

        // Tạo đối tượng tin nhắn của Shop
        ChatMessage welcomeMsg = ChatMessage.builder()
                .senderId(shopId)
                .recipientId(customerId)
                .content(welcomeContent)
                .build();

        // Lưu vào Database (Để khách load lại lịch sử vẫn thấy lời chào)
        ChatMessage savedMsg = chatService.processAndSave(welcomeMsg);

        // Gửi trả về cho người dùng
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(savedMsg)));
        
        // Đánh dấu là đã chào xong
        welcomedUsers.add(customerId);
        log.info("[WELCOME] Đã tự động chào khách hàng: {}", customerId);
    }

    private void handlePrivateMessage(ChatMessage chatMessage) throws Exception {
        // Lưu tin nhắn của khách vào DB
        ChatMessage savedMsg = chatService.processAndSave(chatMessage);
        
        // Tìm "ống nước" của người nhận
        WebSocketSession recipientSession = sessions.get(chatMessage.getRecipientId());
        
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(savedMsg)));
            log.info("[MESSAGE] Đã chuyển tin từ {} tới {}", chatMessage.getSenderId(), chatMessage.getRecipientId());
        } else {
            log.warn("[OFFLINE] Người nhận {} hiện không online", chatMessage.getRecipientId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // Dọn dẹp session khi tắt trình duyệt/ngắt kết nối
        sessions.values().remove(session);
        // Có thể giữ lại welcomedUsers nếu muốn khách quay lại trong cùng ngày không bị chào tiếp
        // Hoặc xóa đi nếu muốn mỗi lần F5 là chào lại: welcomedUsers.clear(); 
        log.info("[SOCKET] Đã đóng kết nối.");
    }
}