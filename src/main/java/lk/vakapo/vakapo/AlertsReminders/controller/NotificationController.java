package lk.vakapo.vakapo.AlertsReminders.controller;

import lk.vakapo.vakapo.AlertsReminders.model.Notification;
import lk.vakapo.vakapo.AlertsReminders.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class NotificationController {
    @Autowired
    private NotificationService service;

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("notifications", service.getAllNotifications());
        model.addAttribute("unreadCount", service.getUnreadCount()); // Now resolved
        return "index"; // Refers to index.html in templates
    }

    @PostMapping("/api/notifications")
    public String createNotification(@ModelAttribute Notification notification, Model model) {
        service.createNotification(notification);
        model.addAttribute("notifications", service.getAllNotifications());
        model.addAttribute("unreadCount", service.getUnreadCount()); // Now resolved
        return "redirect:/"; // Redirect to the index page after creation
    }

    @PutMapping("/api/notifications/{id}")
    public String updateNotification(@PathVariable Long id, @ModelAttribute Notification notification, Model model) {
        service.updateNotification(id, notification);
        model.addAttribute("notifications", service.getAllNotifications());
        model.addAttribute("unreadCount", service.getUnreadCount()); // Now resolved
        return "redirect:/";
    }

    @DeleteMapping("/api/notifications/{id}")
    public String deleteNotification(@PathVariable Long id, Model model) {
        service.deleteNotification(id);
        model.addAttribute("notifications", service.getAllNotifications());
        model.addAttribute("unreadCount", service.getUnreadCount()); // Now resolved
        return "redirect:/";
    }

    @PatchMapping("/api/notifications/{id}/read")
    public String markAsRead(@PathVariable Long id, Model model) {
        service.markAsRead(id); // Now resolved
        model.addAttribute("notifications", service.getAllNotifications());
        model.addAttribute("unreadCount", service.getUnreadCount()); // Now resolved
        return "redirect:/"; // Redirect to refresh the page
    }

    @GetMapping("/api/notifications/unread-count")
    public ResponseEntity<Long> getUnreadCountApi() {
        return ResponseEntity.ok(service.getUnreadCount()); // Now resolved
    }
}