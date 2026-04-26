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

    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    private static final Set<String> welcomedUsers = ConcurrentHashMap.newKeySet();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        
        String senderId = chatMessage.getSenderId();
        
        if (!sessions.containsKey(senderId)) {
            sessions.put(senderId, session);
            log.info("[SOCKET] Đã đăng ký định danh cho User: {}", senderId);
        }

       
        if ("OPEN_CHAT".equalsIgnoreCase(chatMessage.getContent()) || !welcomedUsers.contains(senderId)) {
            sendAutoWelcomeMessage(session, senderId);
            
            if ("OPEN_CHAT".equalsIgnoreCase(chatMessage.getContent())) return;
        }

      
        handlePrivateMessage(chatMessage);
    }

    private void sendAutoWelcomeMessage(WebSocketSession session, String customerId) throws Exception {
        String shopId = customerId + "shop";
        String welcomeContent = "Xin chào, đây là " + shopId + "! có thể giúp gì cho bạn?";

        
        ChatMessage welcomeMsg = ChatMessage.builder()
                .senderId(shopId)
                .recipientId(customerId)
                .content(welcomeContent)
                .build();

       
        ChatMessage savedMsg = chatService.processAndSave(welcomeMsg);

        
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(savedMsg)));
        
        
        welcomedUsers.add(customerId);
        log.info("[WELCOME] Đã tự động chào khách hàng: {}", customerId);
    }

    private void handlePrivateMessage(ChatMessage chatMessage) throws Exception {
        
        ChatMessage savedMsg = chatService.processAndSave(chatMessage);
        
       
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
       
        sessions.values().remove(session);
         
        log.info("[SOCKET] Đã đóng kết nối.");
    }
}