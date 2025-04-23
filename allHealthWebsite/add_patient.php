<?php
session_start();
require_once 'config.php';

// Check if user is admin
$stmt = $pdo->prepare("SELECT * FROM users WHERE id = ? AND patient_id IS NULL");
$stmt->execute([$_SESSION['user_id']]);
$admin = $stmt->fetch();

if (!$admin) {
    header("Location: index.php");
    exit();
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    try {
        $pdo->beginTransaction();

        // Insert patient
        $stmt = $pdo->prepare("INSERT INTO patients (first_name, second_name) VALUES (?, ?)");
        $stmt->execute([$_POST['first_name'], $_POST['second_name']]);
        $patient_id = $pdo->lastInsertId();

        // Insert user
        $stmt = $pdo->prepare("INSERT INTO users (username, password, patient_id) VALUES (?, ?, ?)");
        $stmt->execute([$_POST['username'], $_POST['password'], $patient_id]);

        // Initialize diagnoses with empty values if not provided
        $stmt = $pdo->prepare("INSERT INTO diagnoses (patient_id, diagnosis, date) VALUES (?, ?, ?)");
        $stmt->execute([$patient_id, $_POST['diagnosis'] ?? 'No diagnoses recorded', $_POST['diagnosis_date'] ?? date('Y-m-d')]);

        // Initialize allergies with empty values if not provided
        $stmt = $pdo->prepare("INSERT INTO allergies (patient_id, allergy, severity) VALUES (?, ?, ?)");
        $stmt->execute([$patient_id, $_POST['allergy'] ?? 'No allergies recorded', $_POST['severity'] ?? 'Mild']);

        // Initialize medications with empty values if not provided
        $stmt = $pdo->prepare("INSERT INTO medications (patient_id, medication_name, dosage) VALUES (?, ?, ?)");
        $stmt->execute([$patient_id, $_POST['medication'] ?? 'No medications recorded', $_POST['dosage'] ?? 'N/A']);

        // Initialize results with empty values if not provided
        $stmt = $pdo->prepare("INSERT INTO results (patient_id, result_description, date) VALUES (?, ?, ?)");
        $stmt->execute([$patient_id, $_POST['result'] ?? 'No test results recorded', $_POST['result_date'] ?? date('Y-m-d')]);

        // Initialize vaccines with empty values if not provided
        $stmt = $pdo->prepare("INSERT INTO vaccines (patient_id, vaccine_name, date_administered) VALUES (?, ?, ?)");
        $stmt->execute([$patient_id, $_POST['vaccine'] ?? 'No vaccines recorded', $_POST['vaccine_date'] ?? date('Y-m-d')]);

        $pdo->commit();
        header("Location: admin_dashboard.php?success=1");
        exit();
    } catch (Exception $e) {
        $pdo->rollBack();
        $error = "Error adding patient: " . $e->getMessage();
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AllHealth - Add Patient</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="dashboard-container">
        <header>
            <h1>Add New Patient</h1>
            <div>
                <a href="admin_dashboard.php" class="btn-secondary">Back to Dashboard</a>
                <a href="logout.php" class="logout-btn">Logout</a>
            </div>
        </header>

        <?php if (isset($error)): ?>
            <div class="error-message"><?php echo $error; ?></div>
        <?php endif; ?>

        <form action="add_patient.php" method="POST" class="patient-form">
            <div class="form-section">
                <h2>Basic Information</h2>
                <div class="input-group">
                    <label for="first_name">First Name</label>
                    <input type="text" id="first_name" name="first_name" required>
                </div>
                <div class="input-group">
                    <label for="second_name">Second Name</label>
                    <input type="text" id="second_name" name="second_name" required>
                </div>
                <div class="input-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" required>
                </div>
                <div class="input-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" required>
                </div>
            </div>

            <div class="form-section">
                <h2>Medical Information</h2>
                <div class="input-group">
                    <label for="diagnosis">Diagnosis</label>
                    <input type="text" id="diagnosis" name="diagnosis">
                </div>
                <div class="input-group">
                    <label for="diagnosis_date">Diagnosis Date</label>
                    <input type="date" id="diagnosis_date" name="diagnosis_date">
                </div>

                <div class="input-group">
                    <label for="allergy">Allergy</label>
                    <input type="text" id="allergy" name="allergy">
                </div>
                <div class="input-group">
                    <label for="severity">Severity</label>
                    <select id="severity" name="severity">
                        <option value="Mild">Mild</option>
                        <option value="Moderate">Moderate</option>
                        <option value="Severe">Severe</option>
                    </select>
                </div>

                <div class="input-group">
                    <label for="medication">Medication</label>
                    <input type="text" id="medication" name="medication">
                </div>
                <div class="input-group">
                    <label for="dosage">Dosage</label>
                    <input type="text" id="dosage" name="dosage">
                </div>

                <div class="input-group">
                    <label for="result">Test Result</label>
                    <input type="text" id="result" name="result">
                </div>
                <div class="input-group">
                    <label for="result_date">Result Date</label>
                    <input type="date" id="result_date" name="result_date">
                </div>

                <div class="input-group">
                    <label for="vaccine">Vaccine</label>
                    <input type="text" id="vaccine" name="vaccine">
                </div>
                <div class="input-group">
                    <label for="vaccine_date">Vaccine Date</label>
                    <input type="date" id="vaccine_date" name="vaccine_date">
                </div>
            </div>

            <button type="submit" class="btn-primary">Add Patient</button>
        </form>
    </div>
</body>
</html> 