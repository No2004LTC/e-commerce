package ecommerce.example.ecommerce.adapter.persistence.chat;

import ecommerce.example.ecommerce.domain.chat.ChatMessage;
import ecommerce.example.ecommerce.domain.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Anti-Corruption Layer: maps between ChatMessage (domain) and ChatMessageEntity (JPA).
 * Handles timestamp format conversion and enriched metadata fields.
 */
@Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {

    private final ChatJpaRepository jpaRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void save(ChatMessage message) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .senderId(message.getSenderId())
                .recipientId(message.getRecipientId())
                .content(message.getContent())
                .timestamp(message.getTimestamp() != null
                        ? LocalDateTime.parse(message.getTimestamp(), FORMATTER)
                        : LocalDateTime.now())
                // Rich metadata (may be null for legacy messages saved before enrichment)
                .senderName(message.getSenderName())
                .senderRole(message.getSenderRole())
                .branchLabel(message.getBranchLabel())
                .build();
        jpaRepository.save(entity);
    }

    @Override
    public List<ChatMessage> getHistory(String user1, String user2) {
        return jpaRepository
                .findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
                        user1, user2, user2, user1)
                .stream()
                .map(entity -> ChatMessage.builder()
                        .id(entity.getId() != null ? entity.getId().toString() : null)
                        .senderId(entity.getSenderId())
                        .recipientId(entity.getRecipientId())
                        .content(entity.getContent())
                        .timestamp(entity.getTimestamp() != null
                                ? entity.getTimestamp().format(FORMATTER) : null)
                        .senderName(entity.getSenderName())
                        .senderRole(entity.getSenderRole())
                        .branchLabel(entity.getBranchLabel())
                        .build())
                .collect(Collectors.toList());
    }
}