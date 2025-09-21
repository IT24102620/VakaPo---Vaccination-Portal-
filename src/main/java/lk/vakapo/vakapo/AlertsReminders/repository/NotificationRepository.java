package lk.vakapo.vakapo.AlertsReminders.repository;

import lk.vakapo.vakapo.AlertsReminders.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByIsReadFalse(); // This enables getUnreadCount()
}