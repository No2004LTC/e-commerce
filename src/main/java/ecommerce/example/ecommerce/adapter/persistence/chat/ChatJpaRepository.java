package ecommerce.example.ecommerce.adapter.persistence.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatJpaRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findBySenderIdAndRecipientIdOrSenderIdAndRecipientIdOrderByTimestampAsc(
        String s1, String r1, String s2, String r2
    );
}