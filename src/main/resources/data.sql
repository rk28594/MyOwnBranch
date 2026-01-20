-- Sample data for Patient Management System
-- This SQL script pre-fills the database with sample patients for demo purposes

INSERT INTO patients (first_name, last_name, date_of_birth, gender, department, is_critical, admitted_at)
VALUES ('John', 'Doe', '1985-05-12', 'MALE', 'Cardiology', false, CURRENT_TIMESTAMP);

INSERT INTO patients (first_name, last_name, date_of_birth, gender, department, is_critical, admitted_at)
VALUES ('Sarah', 'Smith', '1992-10-24', 'FEMALE', 'Emergency', true, CURRENT_TIMESTAMP);

INSERT INTO patients (first_name, last_name, date_of_birth, gender, department, is_critical, admitted_at)
VALUES ('Michael', 'Johnson', '1978-03-15', 'MALE', 'Pediatrics', false, CURRENT_TIMESTAMP);

INSERT INTO patients (first_name, last_name, date_of_birth, gender, department, is_critical, admitted_at)
VALUES ('Emily', 'Davis', '2000-07-30', 'FEMALE', 'Cardiology', true, CURRENT_TIMESTAMP);

INSERT INTO patients (first_name, last_name, date_of_birth, gender, department, is_critical, admitted_at)
VALUES ('Robert', 'Brown', '1965-12-01', 'MALE', 'Orthopedics', false, CURRENT_TIMESTAMP);
