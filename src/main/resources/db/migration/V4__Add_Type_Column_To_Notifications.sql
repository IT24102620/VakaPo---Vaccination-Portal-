-- Add type column to notifications table
ALTER TABLE dbo.notifications 
ADD type NVARCHAR(50) NOT NULL DEFAULT 'notification';

-- Create index for better performance on type queries
CREATE INDEX idx_notifications_type ON dbo.notifications(type);
