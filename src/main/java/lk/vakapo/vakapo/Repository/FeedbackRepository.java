package lk.vakapo.vakapo.Repository;

import lk.vakapo.vakapo.Model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByStatus(Feedback.Status status);
}
