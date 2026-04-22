package ecommerce.example.ecommerce.application.chat;

import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import ecommerce.example.ecommerce.domain.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public ChatMessage processAndSave(ChatMessage message) {
        // Gán thời gian hiện tại nếu chưa có
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now().toString());
        }
        chatRepository.save(message);
        return message;
    }

    public List<ChatMessage> getHistory(String u1, String u2) {
        return chatRepository.getHistory(u1, u2);
    }
}