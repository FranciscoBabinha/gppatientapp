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

$patient_id = $_GET['id'] ?? null;

if (!$patient_id) {
    header("Location: admin_dashboard.php");
    exit();
}

// Fetch patient information
$stmt = $pdo->prepare("SELECT * FROM patients WHERE id = ?");
$stmt->execute([$patient_id]);
$patient = $stmt->fetch();

if (!$patient) {
    header("Location: admin_dashboard.php");
    exit();
}

// Fetch all related information
$stmt = $pdo->prepare("SELECT * FROM diagnoses WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$diagnoses = $stmt->fetchAll();

$stmt = $pdo->prepare("SELECT * FROM allergies WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$allergies = $stmt->fetchAll();

$stmt = $pdo->prepare("SELECT * FROM medications WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$medications = $stmt->fetchAll();

$stmt = $pdo->prepare("SELECT * FROM results WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$results = $stmt->fetchAll();

$stmt = $pdo->prepare("SELECT * FROM vaccines WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$vaccines = $stmt->fetchAll();

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    try {
        $pdo->beginTransaction();

        // Update patient
        $stmt = $pdo->prepare("UPDATE patients SET first_name = ?, second_name = ? WHERE id = ?");
        $stmt->execute([$_POST['first_name'], $_POST['second_name'], $patient_id]);

        // Update diagnoses
        $pdo->prepare("DELETE FROM diagnoses WHERE patient_id = ?")->execute([$patient_id]);
        if (!empty($_POST['diagnosis'])) {
            $stmt = $pdo->prepare("INSERT INTO diagnoses (patient_id, diagnosis, date) VALUES (?, ?, ?)");
            $stmt->execute([$patient_id, $_POST['diagnosis'], $_POST['diagnosis_date']]);
        }

        // Update allergies
        $pdo->prepare("DELETE FROM allergies WHERE patient_id = ?")->execute([$patient_id]);
        if (!empty($_POST['allergy'])) {
            $stmt = $pdo->prepare("INSERT INTO allergies (patient_id, allergy, severity) VALUES (?, ?, ?)");
            $stmt->execute([$patient_id, $_POST['allergy'], $_POST['severity']]);
        }

        // Update medications
        $pdo->prepare("DELETE FROM medications WHERE patient_id = ?")->execute([$patient_id]);
        if (!empty($_POST['medication'])) {
            $stmt = $pdo->prepare("INSERT INTO medications (patient_id, medication_name, dosage) VALUES (?, ?, ?)");
            $stmt->execute([$patient_id, $_POST['medication'], $_POST['dosage']]);
        }

        // Update results
        $pdo->prepare("DELETE FROM results WHERE patient_id = ?")->execute([$patient_id]);
        if (!empty($_POST['result'])) {
            $stmt = $pdo->prepare("INSERT INTO results (patient_id, result_description, date) VALUES (?, ?, ?)");
            $stmt->execute([$patient_id, $_POST['result'], $_POST['result_date']]);
        }

        // Update vaccines
        $pdo->prepare("DELETE FROM vaccines WHERE patient_id = ?")->execute([$patient_id]);
        if (!empty($_POST['vaccine'])) {
            $stmt = $pdo->prepare("INSERT INTO vaccines (patient_id, vaccine_name, date_administered) VALUES (?, ?, ?)");
            $stmt->execute([$patient_id, $_POST['vaccine'], $_POST['vaccine_date']]);
        }

        $pdo->commit();
        header("Location: admin_dashboard.php?success=1");
        exit();
    } catch (Exception $e) {
        $pdo->rollBack();
        $error = "Error updating patient: " . $e->getMessage();
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AllHealth - Edit Patient</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="dashboard-container">
        <header>
            <h1>Edit Patient</h1>
            <div>
                <a href="admin_dashboard.php" class="btn-secondary">Back to Dashboard</a>
                <a href="logout.php" class="logout-btn">Logout</a>
            </div>
        </header>

        <?php if (isset($error)): ?>
            <div class="error-message"><?php echo $error; ?></div>
        <?php endif; ?>

        <form action="edit_patient.php?id=<?php echo $patient_id; ?>" method="POST" class="patient-form">
            <div class="form-section">
                <h2>Basic Information</h2>
                <div class="input-group">
                    <label for="first_name">First Name</label>
                    <input type="text" id="first_name" name="first_name" value="<?php echo htmlspecialchars($patient['first_name']); ?>" required>
                </div>
                <div class="input-group">
                    <label for="second_name">Second Name</label>
                    <input type="text" id="second_name" name="second_name" value="<?php echo htmlspecialchars($patient['second_name']); ?>" required>
                </div>
            </div>

            <div class="form-section">
                <h2>Medical Information</h2>
                <div class="input-group">
                    <label for="diagnosis">Diagnosis</label>
                    <input type="text" id="diagnosis" name="diagnosis" value="<?php echo htmlspecialchars($diagnoses[0]['diagnosis'] ?? ''); ?>">
                </div>
                <div class="input-group">
                    <label for="diagnosis_date">Diagnosis Date</label>
                    <input type="date" id="diagnosis_date" name="diagnosis_date" value="<?php echo $diagnoses[0]['date'] ?? ''; ?>">
                </div>

                <div class="input-group">
                    <label for="allergy">Allergy</label>
                    <input type="text" id="allergy" name="allergy" value="<?php echo htmlspecialchars($allergies[0]['allergy'] ?? ''); ?>">
                </div>
                <div class="input-group">
                    <label for="severity">Severity</label>
                    <select id="severity" name="severity">
                        <option value="Mild" <?php echo ($allergies[0]['severity'] ?? '') == 'Mild' ? 'selected' : ''; ?>>Mild</option>
                        <option value="Moderate" <?php echo ($allergies[0]['severity'] ?? '') == 'Moderate' ? 'selected' : ''; ?>>Moderate</option>
                        <option value="Severe" <?php echo ($allergies[0]['severity'] ?? '') == 'Severe' ? 'selected' : ''; ?>>Severe</option>
                    </select>
                </div>

                <div class="input-group">
                    <label for="medication">Medication</label>
                    <input type="text" id="medication" name="medication" value="<?php echo htmlspecialchars($medications[0]['medication_name'] ?? ''); ?>">
                </div>
                <div class="input-group">
                    <label for="dosage">Dosage</label>
                    <input type="text" id="dosage" name="dosage" value="<?php echo htmlspecialchars($medications[0]['dosage'] ?? ''); ?>">
                </div>

                <div class="input-group">
                    <label for="result">Test Result</label>
                    <input type="text" id="result" name="result" value="<?php echo htmlspecialchars($results[0]['result_description'] ?? ''); ?>">
                </div>
                <div class="input-group">
                    <label for="result_date">Result Date</label>
                    <input type="date" id="result_date" name="result_date" value="<?php echo $results[0]['date'] ?? ''; ?>">
                </div>

                <div class="input-group">
                    <label for="vaccine">Vaccine</label>
                    <input type="text" id="vaccine" name="vaccine" value="<?php echo htmlspecialchars($vaccines[0]['vaccine_name'] ?? ''); ?>">
                </div>
                <div class="input-group">
                    <label for="vaccine_date">Vaccine Date</label>
                    <input type="date" id="vaccine_date" name="vaccine_date" value="<?php echo $vaccines[0]['date_administered'] ?? ''; ?>">
                </div>
            </div>

            <button type="submit" class="btn-primary">Update Patient</button>
        </form>
    </div>
</body>
</html> 