-- Create user_notifications table
CREATE TABLE user_notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id NVARCHAR(20) NOT NULL,
    user_email NVARCHAR(150) NOT NULL,
    user_role NVARCHAR(20) NOT NULL,
    notification_id BIGINT NOT NULL,
    title NVARCHAR(255) NOT NULL,
    message NTEXT NOT NULL,
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    read_at DATETIME2 NULL
);

-- Create indexes for better performance
CREATE INDEX idx_user_notifications_user_id ON user_notifications(user_id);
CREATE INDEX idx_user_notifications_user_email ON user_notifications(user_email);
CREATE INDEX idx_user_notifications_is_read ON user_notifications(is_read);
CREATE INDEX idx_user_notifications_created_at ON user_notifications(created_at);
CREATE INDEX idx_user_notifications_notification_id ON user_notifications(notification_id);
