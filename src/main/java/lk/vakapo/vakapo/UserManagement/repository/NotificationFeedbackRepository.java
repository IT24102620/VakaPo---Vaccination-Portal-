package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.NotificationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationFeedbackRepository extends JpaRepository<NotificationFeedback, Long> {

    // Find all active notifications
    List<NotificationFeedback> findByIsActiveTrueOrderByCreatedAtDesc();

    // Find notifications by target audience
    @Query("SELECT n FROM NotificationFeedback n WHERE n.isActive = true AND " +
           "(n.targetAudience LIKE %:audience% OR n.targetAudience = 'ALL') " +
           "ORDER BY n.createdAt DESC")
    List<NotificationFeedback> findActiveNotificationsForAudience(@Param("audience") String audience);

    // Find notifications created by specific user
    List<NotificationFeedback> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    // Count active notifications
    long countByIsActiveTrue();

    // Find latest emergency notification (get the most recent one)
    @Query("SELECT n FROM NotificationFeedback n WHERE n.isActive = true AND n.type = 'emergency' ORDER BY n.createdAt DESC")
    List<NotificationFeedback> findEmergencyNotifications();
    
    // Find all emergency notifications for cleanup
    @Query("SELECT n FROM NotificationFeedback n WHERE n.isActive = true AND n.type = 'emergency'")
    List<NotificationFeedback> findAllEmergencyNotifications();
}
