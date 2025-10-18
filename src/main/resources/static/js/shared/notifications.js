// Shared notification functionality for all user pages
class NotificationManager {
    constructor() {
        this.notificationIcon = null;
        this.notificationDropdown = null;
        this.unreadCount = 0;
        this.notifications = [];
        this.init();
    }

    init() {
        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setupNotifications());
        } else {
            this.setupNotifications();
        }
    }

    setupNotifications() {
        // Find notification elements
        this.notificationIcon = document.querySelector('.notification-icon');
        this.notificationDropdown = document.querySelector('.notification-dropdown');
        
        if (!this.notificationIcon || !this.notificationDropdown) {
            console.log('Notification elements not found on this page');
            return;
        }

        // Add click event to notification icon
        this.notificationIcon.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation(); // Prevent event bubbling to parent elements
            this.toggleDropdown();
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!this.notificationIcon.contains(e.target) && !this.notificationDropdown.contains(e.target)) {
                this.closeDropdown();
            }
        });

        // Prevent dropdown clicks from bubbling up
        this.notificationDropdown.addEventListener('click', (e) => {
            e.stopPropagation();
        });

        // Load initial notifications
        this.loadNotifications();
        
        // Set up periodic refresh (every 30 seconds)
        setInterval(() => this.loadNotifications(), 30000);
    }

    async loadNotifications() {
        try {
            const response = await fetch('/api/notifications/user/unread');
            const data = await response.json();
            
            if (data.success) {
                this.notifications = data.notifications || [];
                this.unreadCount = data.unreadCount || 0;
                this.updateUI();
            }
        } catch (error) {
            console.error('Error loading notifications:', error);
        }
    }

    updateUI() {
        // Update unread count badge
        this.updateUnreadBadge();
        
        // Update dropdown content
        this.updateDropdownContent();
    }

    updateUnreadBadge() {
        const badge = this.notificationIcon.querySelector('.notification-badge');
        
        if (this.unreadCount > 0) {
            if (!badge) {
                // Create badge if it doesn't exist
                const newBadge = document.createElement('span');
                newBadge.className = 'notification-badge';
                newBadge.style.cssText = `
                    position: absolute;
                    top: -5px;
                    right: -5px;
                    background-color: #ff4444;
                    color: white;
                    border-radius: 50%;
                    width: 18px;
                    height: 18px;
                    font-size: 10px;
                    font-weight: bold;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 1000;
                `;
                this.notificationIcon.style.position = 'relative';
                this.notificationIcon.appendChild(newBadge);
            }
            
            const badgeElement = this.notificationIcon.querySelector('.notification-badge');
            badgeElement.textContent = this.unreadCount > 99 ? '99+' : this.unreadCount;
            badgeElement.style.display = 'flex';
        } else {
            // Hide badge if no unread notifications
            const badge = this.notificationIcon.querySelector('.notification-badge');
            if (badge) {
                badge.style.display = 'none';
            }
        }
    }

    updateDropdownContent() {
        const content = this.notificationDropdown.querySelector('.notification-content');
        if (!content) return;

        if (this.notifications.length === 0) {
            content.innerHTML = `
                <div class="notification-item no-notifications">
                    <p>No new notifications</p>
                </div>
            `;
            return;
        }

        // Show recent notifications (max 5)
        const recentNotifications = this.notifications.slice(0, 5);
        content.innerHTML = recentNotifications.map(notification => `
            <div class="notification-item ${!notification.isRead ? 'unread' : ''}" 
                 onclick="notificationManager.markAsRead(${notification.id})">
                <div class="notification-title">${this.escapeHtml(notification.title)}</div>
                <div class="notification-message">${this.escapeHtml(notification.message.substring(0, 100))}${notification.message.length > 100 ? '...' : ''}</div>
                <div class="notification-time">${this.formatTime(notification.createdAt)}</div>
            </div>
        `).join('');

        // Add "View all" link if there are more notifications
        if (this.notifications.length > 5) {
            content.innerHTML += `
                <div class="notification-footer">
                    <a href="#" onclick="notificationManager.viewAllNotifications()">View all notifications</a>
                </div>
            `;
        }
    }

    toggleDropdown() {
        if (this.notificationDropdown.style.display === 'block') {
            this.closeDropdown();
        } else {
            this.openDropdown();
        }
    }

    openDropdown() {
        this.notificationDropdown.style.display = 'block';
        this.loadNotifications(); // Refresh when opening
    }

    closeDropdown() {
        this.notificationDropdown.style.display = 'none';
    }

    async markAsRead(notificationId) {
        try {
            const response = await fetch(`/api/notifications/mark-read/${notificationId}`, {
                method: 'POST'
            });
            
            if (response.ok) {
                // Update local state
                const notification = this.notifications.find(n => n.id === notificationId);
                if (notification) {
                    notification.isRead = true;
                    this.unreadCount = Math.max(0, this.unreadCount - 1);
                    this.updateUI();
                }
            }
        } catch (error) {
            console.error('Error marking notification as read:', error);
        }
    }

    async markAllAsRead() {
        try {
            const response = await fetch('/api/notifications/mark-all-read', {
                method: 'POST'
            });
            
            if (response.ok) {
                // Update local state
                this.notifications.forEach(notification => {
                    notification.isRead = true;
                });
                this.unreadCount = 0;
                this.updateUI();
            }
        } catch (error) {
            console.error('Error marking all notifications as read:', error);
        }
    }

    viewAllNotifications() {
        // Open the dedicated notifications page
        window.location.href = '/api/notifications/page';
    }

    formatTime(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) { // Less than 1 minute
            return 'Just now';
        } else if (diff < 3600000) { // Less than 1 hour
            return Math.floor(diff / 60000) + ' minutes ago';
        } else if (diff < 86400000) { // Less than 1 day
            return Math.floor(diff / 3600000) + ' hours ago';
        } else {
            return date.toLocaleDateString();
        }
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize notification manager when DOM is ready
let notificationManager;
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        notificationManager = new NotificationManager();
    });
} else {
    notificationManager = new NotificationManager();
}