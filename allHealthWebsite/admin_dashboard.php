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

// Fetch all patients
$stmt = $pdo->query("SELECT * FROM patients ORDER BY first_name, second_name");
$patients = $stmt->fetchAll();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AllHealth - Admin Dashboard</title>
    <link rel="stylesheet" href="styles.css">
</head>
<body>
    <div class="dashboard-container">
        <header>
            <h1>Admin Dashboard</h1>
            <div>
                <a href="add_patient.php" class="btn-primary">Add New Patient</a>
                <a href="logout.php" class="logout-btn">Logout</a>
            </div>
        </header>

        <div class="patients-list">
            <h2>Patients</h2>
            <?php if (count($patients) > 0): ?>
                <table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php foreach ($patients as $patient): ?>
                            <tr>
                                <td>
                                    <a href="edit_patient.php?id=<?php echo $patient['id']; ?>" class="patient-link">
                                        <?php echo htmlspecialchars($patient['first_name'] . ' ' . $patient['second_name']); ?>
                                    </a>
                                </td>
                                <td>
                                    <a href="edit_patient.php?id=<?php echo $patient['id']; ?>" class="btn-edit">Edit</a>
                                    <a href="delete_patient.php?id=<?php echo $patient['id']; ?>" class="btn-delete" onclick="return confirm('Are you sure you want to delete this patient?')">Delete</a>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                    </tbody>
                </table>
            <?php else: ?>
                <p>No patients found.</p>
            <?php endif; ?>
        </div>
    </div>
</body>
</html> 