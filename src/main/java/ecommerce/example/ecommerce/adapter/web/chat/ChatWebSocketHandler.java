package ecommerce.example.ecommerce.adapter.web.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecommerce.example.ecommerce.application.chat.ChatService;
import ecommerce.example.ecommerce.application.chat.AiChatbotService;
import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import ecommerce.example.ecommerce.adapter.security.JwtTokenProvider;
import ecommerce.example.ecommerce.domain.user.UserRepository;
import ecommerce.example.ecommerce.domain.user.User;
import ecommerce.example.ecommerce.adapter.persistence.chat.ChatJpaRepository;
import ecommerce.example.ecommerce.adapter.persistence.chat.ChatMessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AiChatbotService aiChatbotService;
    private final ChatJpaRepository chatJpaRepository;

    // key = email/username, value = WebSocketSession
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // key = admin email, value = WebSocketSession (supports multiple admins)
    private static final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

    private static final Set<String> welcomedUsers = ConcurrentHashMap.newKeySet();

    // ── Helper: resolve User from email/username/UUID ─────────────────
    private java.util.Optional<User> resolveUser(String identity) {
        if (identity == null || identity.isBlank()) return java.util.Optional.empty();
        try {
            return userRepository.findByEmail(identity)
                    .or(() -> userRepository.findByUsername(identity))
                    .or(() -> {
                        try {
                            java.util.UUID uuid = java.util.UUID.fromString(identity);
                            return userRepository.findById(
                                new ecommerce.example.ecommerce.domain.user.UserId(uuid.toString()));
                        } catch (IllegalArgumentException ex) {
                            return java.util.Optional.empty();
                        }
                    });
        } catch (Exception e) {
            log.debug("[SOCKET] resolveUser failed for {}: {}", identity, e.getMessage());
            return java.util.Optional.empty();
        }
    }

    // ── Helper: persist message with full metadata ────────────────────
    private ChatMessageEntity saveChatMessage(String sender, String receiver, String content) {
        // Enrich sender metadata
        String senderName  = sender;
        String senderRole  = "UNKNOWN";
        String branchLabel = null;

        if ("CHATBOT".equalsIgnoreCase(sender)) {
            senderName  = "Trợ lý AI MarketHub";
            senderRole  = "CHATBOT";
            branchLabel = "Hệ thống";
        } else if ("ADMIN".equalsIgnoreCase(sender)) {
            senderName  = "Admin";
            senderRole  = "ROLE_ADMIN";
            branchLabel = "Admin";
        } else {
            var userOpt = resolveUser(sender);
            if (userOpt.isPresent()) {
                User u = userOpt.get();
                senderName  = (u.getFullName() != null && !u.getFullName().isBlank())
                              ? u.getFullName() : u.getUsername();
                senderRole  = u.getRole() != null ? u.getRole().getName() : "UNKNOWN";
                // Branch label: own username + parent chain label
                String ownLabel = u.getUsername();
                if (u.getParentId() != null) {
                    try {
                        var parentOpt = userRepository.findById(
                                new ecommerce.example.ecommerce.domain.user.UserId(u.getParentId()));
                        parentOpt.ifPresent(p -> {
                            // stored in a final var so lambda can use it
                        });
                        String parentLabel = parentOpt.map(p ->
                                (p.getFullName() != null && !p.getFullName().isBlank())
                                ? p.getFullName() : p.getUsername()).orElse(null);
                        branchLabel = parentLabel != null ? parentLabel + " / " + ownLabel : ownLabel;
                    } catch (Exception ex) {
                        branchLabel = ownLabel;
                    }
                } else {
                    branchLabel = ownLabel + " (Chi nhánh chính)";
                }
            }
        }

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .senderId(sender)
                .recipientId(receiver)
                .content(content)
                .timestamp(LocalDateTime.now())
                .senderName(senderName)
                .senderRole(senderRole)
                .branchLabel(branchLabel)
                .build();
        return chatJpaRepository.save(entity);
    }

    // ── Helper: safe JSON send (ignores closed session) ──────────────
    private void safeSend(WebSocketSession session, Object payload) {
        try {
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (IOException e) {
            log.error("[SOCKET] Lỗi gửi tin nhắn qua WS session {}: {}", session != null ? session.getId() : "null", e.getMessage());
        }
    }

    // ── Helper: extract token from query string ──────────────────────
    private String extractToken(WebSocketSession session) {
        try {
            String query = session.getUri() != null ? session.getUri().getQuery() : null;
            if (query != null && query.contains("token=")) {
                String token = query.split("token=")[1];
                if (token.contains("&")) token = token.split("&")[0];
                return java.net.URLDecoder.decode(token, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("[SOCKET] Không thể trích xuất token từ URI: {}", e.getMessage());
        }
        return null;
    }

    private String getRoleFromSession(WebSocketSession session) {
        try {
            String token = extractToken(session);
            if (token != null && jwtTokenProvider.isTokenValid(token)) {
                return jwtTokenProvider.extractRole(token);
            }
        } catch (Exception e) {
            log.error("[SOCKET] Lỗi trích xuất role: {}", e.getMessage());
        }
        return null;
    }

    private String getUsernameFromSession(WebSocketSession session) {
        try {
            String token = extractToken(session);
            if (token != null && jwtTokenProvider.isTokenValid(token)) {
                return jwtTokenProvider.extractUsername(token);
            }
        } catch (Exception e) {
            log.error("[SOCKET] Lỗi trích xuất username: {}", e.getMessage());
        }
        return null;
    }

    // ── Helper: check if a role string is ADMIN ──────────────────────
    private boolean isAdminRole(String role) {
        return "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    // ── Helper: register session into correct maps ───────────────────
    private boolean registerSession(WebSocketSession session, String username, String role) {
        if (username == null || username.isBlank()) return false;

        sessions.put(username, session);

        // Fast path: role already confirmed from JWT
        if (isAdminRole(role)) {
            adminSessions.put(username, session);
            log.info("[SOCKET] Đăng ký Admin session: {} (từ JWT)", username);
            return true;
        }

        // Slow path: verify via DB if JWT role is missing/unknown
        try {
            var userOpt = userRepository.findByEmail(username)
                    .or(() -> userRepository.findByUsername(username));
            if (userOpt.isPresent() && userOpt.get().getRole() != null
                    && isAdminRole(userOpt.get().getRole().getName())) {
                adminSessions.put(username, session);
                log.info("[SOCKET] Đăng ký Admin session: {} (từ DB)", username);
                return true;
            }
        } catch (Exception e) {
            log.warn("[SOCKET] Không thể tra cứu role từ DB cho {}: {}", username, e.getMessage());
        }

        log.info("[SOCKET] Đăng ký User session: {}", username);
        return false;
    }

    // ── Get first available admin session ────────────────────────────
    private WebSocketSession getActiveAdminSession() {
        for (WebSocketSession ws : adminSessions.values()) {
            if (ws != null && ws.isOpen()) return ws;
        }
        return null;
    }

    private String getAdminEmail(WebSocketSession adminSession) {
        if (adminSession == null) return "ADMIN";
        for (Map.Entry<String, WebSocketSession> e : adminSessions.entrySet()) {
            if (e.getValue() == adminSession) return e.getKey();
        }
        return "ADMIN";
    }

    // ════════════════════════════════════════════════════════════════
    // Connection Lifecycle
    // ════════════════════════════════════════════════════════════════

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            String username = getUsernameFromSession(session);
            String role = getRoleFromSession(session);
            registerSession(session, username, role);
        } catch (Exception e) {
            log.error("[SOCKET] afterConnectionEstablished ngoại lệ: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        try {
            sessions.values().remove(session);
            adminSessions.values().remove(session);
            welcomedUsers.removeIf(u -> {
                WebSocketSession s = sessions.get(u);
                return s == null || !s.isOpen();
            });
            log.info("[SOCKET] Đã đóng kết nối. Status: {}", status);
        } catch (Exception e) {
            log.error("[SOCKET] afterConnectionClosed ngoại lệ: {}", e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════
    // Message Handling — Global try-catch prevents session thread crash
    // ════════════════════════════════════════════════════════════════

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            handleTextMessageInternal(session, message);
        } catch (Exception e) {
            log.error("[SOCKET] *** Lỗi nghiêm trọng trong handleTextMessage, đã chặn crash: {}", e.getMessage(), e);
            // Do not rethrow — keeps the session thread alive
        }
    }

    private void handleTextMessageInternal(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("[SOCKET] Nhận tin nhắn: {}", payload);

        Map<String, Object> payloadMap;
        try {
            payloadMap = objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("[SOCKET] Lỗi parse JSON payload: {}", e.getMessage());
            return;
        }

        String target = (String) payloadMap.get("target");
        String messageText = (String) payloadMap.get("message");
        // Also accept "content" field as fallback
        if (messageText == null || messageText.isBlank()) {
            messageText = (String) payloadMap.get("content");
        }
        String senderId = (String) payloadMap.get("senderId");
        String recipientId = (String) payloadMap.get("recipientId");

        // Resolve senderId from JWT if not provided in payload
        if (senderId == null || senderId.isBlank()) {
            senderId = getUsernameFromSession(session);
        }
        if (senderId == null || senderId.isBlank()) {
            log.error("[SOCKET] Không xác định được senderId — bỏ qua tin nhắn");
            return;
        }

        final String finalSenderId = senderId;
        final String finalMessageText = messageText;

        // Register / refresh session on every message
        String role = getRoleFromSession(session);
        boolean senderIsAdmin = registerSession(session, finalSenderId, role);

        // Normalise target
        if ("ADMIN".equalsIgnoreCase(recipientId) || "ADMIN".equalsIgnoreCase(target)) {
            target = "ADMIN";
        }
        if (target == null) {
            target = "CHATBOT";
        }

        // Auto-welcome (only once per session per user)
        if (!welcomedUsers.contains(finalSenderId) && !senderIsAdmin) {
            sendAutoWelcomeMessage(session, finalSenderId);
        }

        // ── Route message ────────────────────────────────────────────
        if ("ADMIN".equalsIgnoreCase(target)) {
            routeToAdmin(session, payloadMap, finalSenderId, finalMessageText, recipientId, senderIsAdmin);
        } else if ("STAFF".equalsIgnoreCase(target)) {
            routeToStaff(payloadMap, finalSenderId, finalMessageText);
        } else {
            // Default: CHATBOT
            routeToChatbot(session, finalSenderId, finalMessageText, role);
        }
    }

    // ── Route: Staff/Branch → Admin ──────────────────────────────────
    private void routeToAdmin(WebSocketSession senderSession,
                               Map<String, Object> payloadMap,
                               String senderId,
                               String messageText,
                               String recipientId,
                               boolean senderIsAdmin) {
        if (senderIsAdmin) {
            // Admin → Branch reply
            if (recipientId != null && !recipientId.isBlank()) {
                ChatMessageEntity entity = saveChatMessage(senderId, recipientId, messageText);
                ChatMessage savedMsg = buildChatMessage(entity);
                WebSocketSession recipientSession = sessions.get(recipientId);
                safeSend(recipientSession, savedMsg);
                log.info("[ADMIN→BRANCH] {} → {}", senderId, recipientId);
            } else {
                log.warn("[ADMIN→BRANCH] recipientId trống, không thể gửi phản hồi");
            }
        } else {
            // Branch → Admin
            WebSocketSession adminSession = getActiveAdminSession();
            String adminEmail = getAdminEmail(adminSession);

            ChatMessageEntity entity = saveChatMessage(senderId, adminEmail, messageText);
            ChatMessage savedMsg = buildChatMessage(entity);

            if (adminSession != null) {
                safeSend(adminSession, savedMsg);
                log.info("[BRANCH→ADMIN] {} → admin ({})", senderId, adminEmail);
            } else {
                log.warn("[BRANCH→ADMIN] Không có Admin online — đã lưu DB: {}", senderId);
            }
        }
    }

    // ── Route: Admin → Staff ─────────────────────────────────────────
    private void routeToStaff(Map<String, Object> payloadMap, String senderId, String messageText) {
        String receiverId = (String) payloadMap.get("receiverId");
        if (receiverId == null || receiverId.isBlank()) {
            log.warn("[ADMIN→STAFF] receiverId trống");
            return;
        }

        String resolvedReceiverId = receiverId;
        try {
            var userOpt = userRepository.findById(
                    new ecommerce.example.ecommerce.domain.user.UserId(receiverId));
            if (userOpt.isPresent()) {
                resolvedReceiverId = userOpt.get().getEmail();
            }
        } catch (Exception e) {
            log.debug("[ADMIN→STAFF] receiverId không phải UUID hợp lệ: {}", receiverId);
        }

        ChatMessageEntity entity = saveChatMessage(senderId, resolvedReceiverId, messageText);
        ChatMessage savedMsg = buildChatMessage(entity);

        WebSocketSession recipientSession = sessions.getOrDefault(resolvedReceiverId, sessions.get(receiverId));
        safeSend(recipientSession, savedMsg);

        if (recipientSession != null && recipientSession.isOpen()) {
            log.info("[ADMIN→STAFF] {} → {}", senderId, resolvedReceiverId);
        } else {
            log.warn("[ADMIN→STAFF] Nhân viên {} offline — đã lưu DB", resolvedReceiverId);
        }
    }

    // ── Route: User → Chatbot ────────────────────────────────────────
    private void routeToChatbot(WebSocketSession session, String senderId, String messageText, String role) {
        boolean isAuthorized = isAdminRole(role) ||
                "ROLE_SHOP_OWNER".equalsIgnoreCase(role) ||
                "ROLE_BRANCH".equalsIgnoreCase(role) ||
                "ROLE_STAFF".equalsIgnoreCase(role);

        if (!isAuthorized) {
            try {
                var userOpt = userRepository.findByEmail(senderId).or(() -> userRepository.findByUsername(senderId));
                if (userOpt.isPresent() && userOpt.get().getRole() != null) {
                    String dbRole = userOpt.get().getRole().getName();
                    isAuthorized = isAdminRole(dbRole) ||
                            "ROLE_SHOP_OWNER".equalsIgnoreCase(dbRole) ||
                            "ROLE_BRANCH".equalsIgnoreCase(dbRole) ||
                            "ROLE_STAFF".equalsIgnoreCase(dbRole);
                }
            } catch (Exception e) {
                log.warn("[CHATBOT] Lỗi tra cứu role DB: {}", e.getMessage());
            }
        }

        if (!isAuthorized) {
            log.warn("[CHATBOT] Từ chối truy cập Chatbot AI cho user: {}", senderId);
            return;
        }

        saveChatMessage(senderId, "CHATBOT", messageText);

        String aiResponse;
        try {
            aiResponse = aiChatbotService.getBotResponse(messageText);
        } catch (Exception e) {
            log.error("[CHATBOT] AiChatbotService lỗi: {}", e.getMessage());
            aiResponse = "Xin lỗi, trợ lý AI đang gặp sự cố. Vui lòng thử lại sau.";
        }

        ChatMessageEntity botEntity = saveChatMessage("CHATBOT", senderId, aiResponse);
        ChatMessage savedBotMsg = buildChatMessage(botEntity);
        safeSend(session, savedBotMsg);
        log.info("[CHATBOT] Đã phản hồi cho {}", senderId);
    }

    // ── Auto welcome ─────────────────────────────────────────────────
    private void sendAutoWelcomeMessage(WebSocketSession session, String customerId) {
        try {
            String welcomeContent = "Xin chào! Tôi là Trợ lý kỹ thuật thông minh của POS MarketHub. Tôi có thể giúp gì cho bạn hôm nay?";
            ChatMessageEntity entity = saveChatMessage("CHATBOT", customerId, welcomeContent);
            ChatMessage savedMsg = buildChatMessage(entity);
            safeSend(session, savedMsg);
            welcomedUsers.add(customerId);
            log.info("[WELCOME] Đã chào khách: {}", customerId);
        } catch (Exception e) {
            log.error("[WELCOME] Lỗi gửi tin chào: {}", e.getMessage());
        }
    }

    // ── Build ChatMessage domain from entity (with all metadata) ────────
    private ChatMessage buildChatMessage(ChatMessageEntity entity) {
        return ChatMessage.builder()
                .id(entity.getId() != null ? entity.getId().toString() : null)
                .senderId(entity.getSenderId())
                .recipientId(entity.getRecipientId())
                .content(entity.getContent())
                .timestamp(entity.getTimestamp().toString())
                .type("CHAT")
                .senderName(entity.getSenderName())
                .senderRole(entity.getSenderRole())
                .branchLabel(entity.getBranchLabel())
                .build();
    }
}