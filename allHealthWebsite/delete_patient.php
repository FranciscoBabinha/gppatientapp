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

if ($patient_id) {
    try {
        $pdo->beginTransaction();

        // Delete user account first (to maintain referential integrity)
        $stmt = $pdo->prepare("DELETE FROM users WHERE patient_id = ?");
        $stmt->execute([$patient_id]);

        // Delete patient (this will cascade delete all related records)
        $stmt = $pdo->prepare("DELETE FROM patients WHERE id = ?");
        $stmt->execute([$patient_id]);

        $pdo->commit();
        header("Location: admin_dashboard.php?success=2");
        exit();
    } catch (Exception $e) {
        $pdo->rollBack();
        header("Location: admin_dashboard.php?error=1");
        exit();
    }
} else {
    header("Location: admin_dashboard.php");
    exit();
}
?> 