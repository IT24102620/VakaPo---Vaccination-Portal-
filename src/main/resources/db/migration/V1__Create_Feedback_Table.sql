-- Create feedback table
CREATE TABLE feedback (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NOT NULL,
    contact_no NVARCHAR(50),
    rating INT NOT NULL,
    message NTEXT,
    user_type NVARCHAR(50) NOT NULL,
    user_id NVARCHAR(255),
    is_approved BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    approved_at DATETIME2,
    approved_by NVARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX idx_feedback_approved ON feedback(is_approved);
CREATE INDEX idx_feedback_user_type ON feedback(user_type);
CREATE INDEX idx_feedback_created_at ON feedback(created_at);
CREATE INDEX idx_feedback_user_id ON feedback(user_id);

-- Insert some sample feedback for testing (optional)
INSERT INTO feedback (name, email, contact_no, rating, message, user_type, user_id, is_approved, created_at) VALUES
('Dr. Sarah Johnson', 'sarah.johnson@hospital.com', '+94-77-123-4567', 5, 'VakaPo has revolutionized how I manage my patients. The appointment system is seamless and the patient history tracking is excellent.', 'DOCTOR', 'Vak_D_1001', 1, GETDATE()),
('Nurse Priya Kumar', 'priya.kumar@clinic.com', '+94-77-234-5678', 5, 'Excellent platform for managing vaccination records. The real-time updates help me stay on top of patient care.', 'NURSE', 'Vak_N_1001', 1, GETDATE()),
('Royal Hospital Admin', 'admin@royalhospital.com', '+94-77-345-6789', 5, 'VakaPo has transformed our vaccination management. The staff invitation system and appointment tracking are excellent.', 'HOSPITAL', 'Vak_H_1001', 1, GETDATE()),
('Family Clinic Manager', 'manager@familyclinic.com', '+94-77-456-7890', 4, 'Excellent platform for managing patient records and vaccination schedules. The staff invitation feature works perfectly.', 'CLINIC', 'Vak_C_1001', 1, GETDATE()),
('Patient Kumar Silva', 'kumar.silva@email.com', '+94-77-567-8901', 5, 'VakaPo made booking my vaccination so easy! The appointment system is user-friendly and I received timely reminders.', 'PATIENT', 'Vak_P_1001', 1, GETDATE()),
-- Add some pending feedback for admin testing
('Dr. John Smith', 'john.smith@hospital.com', '+94-77-111-2222', 4, 'The platform is good but could use some improvements in the user interface. Overall satisfied with the service.', 'DOCTOR', 'Vak_D_1002', 0, GETDATE()),
('Nurse Maria Garcia', 'maria.garcia@clinic.com', '+94-77-333-4444', 5, 'Excellent service! The vaccination tracking system is very helpful for managing patient records.', 'NURSE', 'Vak_N_1002', 0, GETDATE()),
('City Hospital Admin', 'admin@cityhospital.com', '+94-77-555-6666', 3, 'The system works well but we need better reporting features for our management team.', 'HOSPITAL', 'Vak_H_1002', 0, GETDATE()),
('Patient Alex Johnson', 'alex.johnson@email.com', '+94-77-777-8888', 5, 'Very easy to book appointments and track my vaccination history. Highly recommended!', 'PATIENT', 'Vak_P_1002', 0, GETDATE());
