package in.technobuild.chatbot.repository;

import in.technobuild.chatbot.entity.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findBySessionId(String sessionId);

    List<Conversation> findByUserIdOrderByLastActiveDesc(Long userId);

    Optional<Conversation> findByUserIdAndSessionId(Long userId, String sessionId);

    void deleteByUserId(Long userId);
}
