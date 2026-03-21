package in.technobuild.chatbot.repository;

import in.technobuild.chatbot.entity.Feedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByUserId(Long userId);

    List<Feedback> findByFlaggedTrue();

    List<Feedback> findByRating(Byte rating);

    List<Feedback> findByConversationId(Long conversationId);
}
