<?php
session_start();
require_once 'config.php';

if (!isset($_SESSION['user_id'])) {
    header("Location: index.php");
    exit();
}

$patient_id = $_SESSION['patient_id'];

// Fetch patient information
$stmt = $pdo->prepare("SELECT * FROM patients WHERE id = ?");
$stmt->execute([$patient_id]);
$patient = $stmt->fetch();

// Fetch diagnoses
$stmt = $pdo->prepare("SELECT * FROM diagnoses WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$diagnoses = $stmt->fetchAll();

// Fetch allergies
$stmt = $pdo->prepare("SELECT * FROM allergies WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$allergies = $stmt->fetchAll();

// Fetch medications
$stmt = $pdo->prepare("SELECT * FROM medications WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$medications = $stmt->fetchAll();

// Fetch results
$stmt = $pdo->prepare("SELECT * FROM results WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$results = $stmt->fetchAll();

// Fetch vaccines
$stmt = $pdo->prepare("SELECT * FROM vaccines WHERE patient_id = ?");
$stmt->execute([$patient_id]);
$vaccines = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AllHealth - Dashboard</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="dashboard-container">
        <header>
            <h1>Welcome, <?php echo htmlspecialchars($_SESSION['first_name'] . ' ' . $_SESSION['second_name']); ?></h1>
            <a href="logout.php" class="logout-btn">Logout</a>
        </header>

        <div class="dashboard-grid">
            <section class="info-card">
                <h2>Personal Information</h2>
                <p><strong>Name:</strong> <?php echo htmlspecialchars($patient['first_name'] . ' ' . $patient['second_name']); ?></p>
            </section>

            <section class="info-card">
                <h2>Diagnoses</h2>
                <?php if (count($diagnoses) > 0): ?>
                    <?php foreach ($diagnoses as $diagnosis): ?>
                        <p><?php echo htmlspecialchars($diagnosis['diagnosis']); ?> (<?php echo $diagnosis['date']; ?>)</p>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p>No diagnoses recorded</p>
                <?php endif; ?>
            </section>

            <section class="info-card">
                <h2>Allergies</h2>
                <?php if (count($allergies) > 0): ?>
                    <?php foreach ($allergies as $allergy): ?>
                        <p><?php echo htmlspecialchars($allergy['allergy']); ?> - <?php echo htmlspecialchars($allergy['severity']); ?></p>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p>No allergies recorded</p>
                <?php endif; ?>
            </section>

            <section class="info-card">
                <h2>Medications</h2>
                <?php if (count($medications) > 0): ?>
                    <?php foreach ($medications as $medication): ?>
                        <p><?php echo htmlspecialchars($medication['medication_name']); ?> - <?php echo htmlspecialchars($medication['dosage']); ?></p>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p>No medications recorded</p>
                <?php endif; ?>
            </section>

            <section class="info-card">
                <h2>Test Results</h2>
                <?php if (count($results) > 0): ?>
                    <?php foreach ($results as $result): ?>
                        <p><?php echo htmlspecialchars($result['result_description']); ?> (<?php echo $result['date']; ?>)</p>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p>No test results recorded</p>
                <?php endif; ?>
            </section>

            <section class="info-card">
                <h2>Vaccines</h2>
                <?php if (count($vaccines) > 0): ?>
                    <?php foreach ($vaccines as $vaccine): ?>
                        <p><?php echo htmlspecialchars($vaccine['vaccine_name']); ?> (<?php echo $vaccine['date_administered']; ?>)</p>
                    <?php endforeach; ?>
                <?php else: ?>
                    <p>No vaccines recorded</p>
                <?php endif; ?>
            </section>
        </div>
    </div>
</body>
</html> 