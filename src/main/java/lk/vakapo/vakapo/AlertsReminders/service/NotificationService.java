package lk.vakapo.vakapo.AlertsReminders.service;

import lk.vakapo.vakapo.AlertsReminders.model.Notification;
import lk.vakapo.vakapo.AlertsReminders.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository repository;

    public List<Notification> getAllNotifications() {
        return repository.findAll();
    }

    public Notification getNotificationById(Long id) {
        Optional<Notification> notification = repository.findById(id);
        return notification.orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
    }

    public Notification createNotification(Notification notification) {
        if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        return repository.save(notification);
    }

    public Notification updateNotification(Long id, Notification updatedNotification) {
        Notification existing = getNotificationById(id);
        existing.setTitle(updatedNotification.getTitle());
        existing.setMessage(updatedNotification.getMessage());
        existing.setAudience(updatedNotification.getAudience());
        existing.setDeliveryMethod(updatedNotification.getDeliveryMethod());
        existing.setScheduleDate(updatedNotification.getScheduleDate());
        existing.setRead(updatedNotification.isRead());
        return repository.save(existing);
    }

    public void deleteNotification(Long id) {
        Notification notification = getNotificationById(id);
        repository.delete(notification);
    }

    // This method was missing or mismatched - it counts unread notifications
    public long getUnreadCount() {
        return repository.countByIsReadFalse();
    }

    // This method was missing or mismatched - marks a notification as read
    public Notification markAsRead(Long id) {
        Notification notification = getNotificationById(id);
        notification.setRead(true);
        return repository.save(notification);
    }
}