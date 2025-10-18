package lk.vakapo.vakapo.UserManagement.repository;

import lk.vakapo.vakapo.UserManagement.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    // Find all notifications for a specific user
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find unread notifications for a specific user
    List<UserNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);

    // Count unread notifications for a specific user
    long countByUserIdAndIsReadFalse(String userId);

    // Find notifications by user email
    List<UserNotification> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    // Find unread notifications by user email
    List<UserNotification> findByUserEmailAndIsReadFalseOrderByCreatedAtDesc(String userEmail);

    // Count unread notifications by user email
    long countByUserEmailAndIsReadFalse(String userEmail);

    // Mark notification as read
    @Modifying
    @Transactional
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    // Mark all notifications as read for a user
    @Modifying
    @Transactional
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") String userId);

    // Mark all notifications as read for a user by email
    @Modifying
    @Transactional
    @Query("UPDATE UserNotification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userEmail = :userEmail AND n.isRead = false")
    void markAllAsReadForUserByEmail(@Param("userEmail") String userEmail);

    // Delete old read notifications (cleanup)
    @Modifying
    @Transactional
    @Query("DELETE FROM UserNotification n WHERE n.isRead = true AND n.readAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
