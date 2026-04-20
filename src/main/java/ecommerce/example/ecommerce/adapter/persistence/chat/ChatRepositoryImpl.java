package ecommerce.example.ecommerce.adapter.persistence.chat;

import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import ecommerce.example.ecommerce.domain.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {

    private final ChatJpaRepository jpaRepository;
    // Định dạng thời gian để chuyển từ String sang LocalDateTime và ngược lại
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void save(ChatMessage message) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .senderId(message.getSenderId())
                .recipientId(message.getRecipientId())
                .content(message.getContent())
                // Chuyển String từ Domain sang LocalDateTime để lưu DB
                .timestamp(message.getTimestamp() != null ? LocalDateTime.parse(message.getTimestamp(), formatter) : LocalDateTime.now())
                .build();
        jpaRepository.save(entity);
    }

    @Override // Phải là getHistory để khớp với Interface
    public List<ChatMessage> getHistory(String user1, String user2) {
        return jpaRepository.findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(user1, user2, user2, user1)
                .stream()
                .map(entity -> ChatMessage.builder()
                        .senderId(entity.getSenderId())
                        .recipientId(entity.getRecipientId())
                        .content(entity.getContent())
                        // Chuyển LocalDateTime từ DB sang String để trả về Domain
                        .timestamp(entity.getTimestamp() != null ? entity.getTimestamp().format(formatter) : null)
                        .build())
                .collect(Collectors.toList());
    }
}