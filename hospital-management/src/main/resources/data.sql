-- Initial test data for Patient Management
-- This data will be loaded when the application starts

-- Insert sample patients
INSERT INTO patients (first_name, last_name, dob, email, phone, created_at, updated_at) 
VALUES ('John', 'Doe', '1985-03-15', 'john.doe@example.com', '+1234567890', CURRENT_DATE, CURRENT_DATE);

INSERT INTO patients (first_name, last_name, dob, email, phone, created_at, updated_at) 
VALUES ('Jane', 'Smith', '1990-07-22', 'jane.smith@example.com', '+1234567891', CURRENT_DATE, CURRENT_DATE);

INSERT INTO patients (first_name, last_name, dob, email, phone, created_at, updated_at) 
VALUES ('Michael', 'Johnson', '1978-11-30', 'michael.johnson@example.com', '+1234567892', CURRENT_DATE, CURRENT_DATE);
