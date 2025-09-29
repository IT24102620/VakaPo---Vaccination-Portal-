package lk.vakapo.vakapo.Repository;


import lk.vakapo.vakapo.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NurseFeedbackRepository extends JpaRepository<NurseFeedback, Long> {
    List<NurseFeedback> findByStatus(FeedbackStatus status);
}
