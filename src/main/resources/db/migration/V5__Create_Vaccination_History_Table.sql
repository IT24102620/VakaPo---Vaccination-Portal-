-- Create vaccination_history table with optimized column sizes
-- This fixes the row size limit issue (8060 bytes max in SQL Server)

CREATE TABLE vaccination_history (
    history_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    patient_id NVARCHAR(20) NOT NULL,
    patient_email NVARCHAR(100) NOT NULL,
    patient_name NVARCHAR(100) NOT NULL,
    vaccine_name NVARCHAR(100) NOT NULL,
    vaccination_date DATE NOT NULL,
    time_slot NVARCHAR(20) NOT NULL,
    location NVARCHAR(100) NOT NULL,
    institution_type NVARCHAR(20) NOT NULL,
    institution_id NVARCHAR(20) NOT NULL,
    doctor_name NVARCHAR(100) NOT NULL,
    dosage_level NVARCHAR(20) DEFAULT '0ml',
    additional_notes NVARCHAR(200) NULL,
    next_vaccine_date DATE NULL,
    nurse_approval NVARCHAR(20) NOT NULL DEFAULT 'pending',
    status NVARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Create indexes for better performance
CREATE INDEX idx_vaccination_history_patient_id ON vaccination_history(patient_id);
CREATE INDEX idx_vaccination_history_patient_email ON vaccination_history(patient_email);
CREATE INDEX idx_vaccination_history_vaccination_date ON vaccination_history(vaccination_date);
CREATE INDEX idx_vaccination_history_doctor_name ON vaccination_history(doctor_name);
CREATE INDEX idx_vaccination_history_institution ON vaccination_history(institution_id, institution_type);
CREATE INDEX idx_vaccination_history_status ON vaccination_history(status);
CREATE INDEX idx_vaccination_history_nurse_approval ON vaccination_history(nurse_approval);





