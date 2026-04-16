package ecommerce.example.ecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Điểm kết nối để client nhảy vào (ví dụ: ws://localhost:8080/ws)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") 
                .withSockJS(); // Hỗ trợ dự phòng cho browser cũ
        
        // Thêm một endpoint không có SockJS để Postman test dễ hơn
        registry.addEndpoint("/ws-raw")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tiền tố gửi tin nhắn từ Client lên Server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Các kênh để Client lắng nghe tin nhắn từ Server đổ về
        registry.enableSimpleBroker("/topic", "/queue");
        
        // Tiền tố cho tin nhắn riêng tư (1-1)
        registry.setUserDestinationPrefix("/user");
    }
}