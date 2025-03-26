CREATE DATABASE IF NOT EXISTS gp_app;
USE gp_app;

-- Patients Table
CREATE TABLE IF NOT EXISTS patients (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL, 
    second_name VARCHAR(50) NOT NULL 
);

-- Diagnoses Table
CREATE TABLE IF NOT EXISTS diagnoses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Allergies Table
CREATE TABLE IF NOT EXISTS allergies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    allergy VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Medications Table
CREATE TABLE IF NOT EXISTS medications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(50) NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Results Table
CREATE TABLE IF NOT EXISTS results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    result_description TEXT NOT NULL,
    date DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Documents Table
CREATE TABLE IF NOT EXISTS documents (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    document_title VARCHAR(255) NOT NULL,
    upload_date DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Vaccines Table
CREATE TABLE IF NOT EXISTS vaccines (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    vaccine_name VARCHAR(255) NOT NULL,
    date_administered DATE NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    patient_id INT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Insert test data
INSERT INTO patients (first_name, second_name)
VALUES ('John', 'Doe');

INSERT INTO diagnoses (patient_id, diagnosis, date)
VALUES (1, 'Hypertension', '2023-01-15');

INSERT INTO allergies (patient_id, allergy, severity)
VALUES (1, 'Peanuts', 'Severe');

INSERT INTO medications (patient_id, medication_name, dosage)
VALUES (1, 'Aspirin', '75mg daily');

INSERT INTO results (patient_id, result_description, date)
VALUES (1, 'Blood test normal', '2023-02-10');

INSERT INTO documents (patient_id, document_title, upload_date)
VALUES (1, 'Prescription', '2023-03-01');

INSERT INTO vaccines (patient_id, vaccine_name, date_administered)
VALUES (1, 'COVID-19 Vaccine', '2022-12-20');

-- Insert test user (password: 1234)
INSERT INTO users (username, password, patient_id)
VALUES ('john', '1234', 1);