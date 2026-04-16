package ecommerce.example.ecommerce.domain.chat;

import java.util.List;

public interface ChatRepository {
    void save(ChatMessage message);
    List<ChatMessage> getHistory(String user1, String user2);
}