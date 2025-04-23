<?php
// Ensure no whitespace or output before PHP tags
session_start();
require_once 'config.php';

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $username = $_POST['username'];
    $password = $_POST['password'];

    try {
        $stmt = $pdo->prepare("SELECT u.*, p.first_name, p.second_name FROM users u 
                              LEFT JOIN patients p ON u.patient_id = p.id 
                              WHERE u.username = ?");
        $stmt->execute([$username]);
        $user = $stmt->fetch();

        if ($user && $password === $user['password']) {
            $_SESSION['user_id'] = $user['id'];
            $_SESSION['username'] = $user['username'];
            
            if ($user['patient_id'] === null) {
                header("Location: admin_dashboard.php");
            } else {
                $_SESSION['patient_id'] = $user['patient_id'];
                // Get patient information
                $stmt = $pdo->prepare("SELECT first_name, second_name FROM patients WHERE id = ?");
                $stmt->execute([$user['patient_id']]);
                $patient = $stmt->fetch();
                
                $_SESSION['first_name'] = $patient['first_name'];
                $_SESSION['second_name'] = $patient['second_name'];
                header("Location: dashboard.php");
            }
            exit();
        } else {
            header("Location: index.php?error=1");
            exit();
        }
    } catch (PDOException $e) {
        error_log("Database error: " . $e->getMessage());
        header("Location: index.php?error=1");
        exit();
    }
}
?> 