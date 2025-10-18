-- Create vaccines table
CREATE TABLE vaccines (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    vaccine_name NVARCHAR(255) NOT NULL UNIQUE,
    is_active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- Create indexes for better performance
CREATE INDEX idx_vaccines_vaccine_name ON vaccines(vaccine_name);
CREATE INDEX idx_vaccines_is_active ON vaccines(is_active);
CREATE INDEX idx_vaccines_created_at ON vaccines(created_at);
