package lk.vakapo.vakapo.Repository;


import lk.vakapo.vakapo.Model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientFeedbackRepository extends JpaRepository<PatientFeedback, Long> {
    List<PatientFeedback> findByStatus(FeedbackStatus status);
}
