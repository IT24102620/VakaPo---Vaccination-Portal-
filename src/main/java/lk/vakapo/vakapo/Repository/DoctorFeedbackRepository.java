package lk.vakapo.vakapo.Repository;

import lk.vakapo.vakapo.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorFeedbackRepository extends JpaRepository<DoctorFeedback, Long> {
    List<DoctorFeedback> findByStatus(FeedbackStatus status);
}
