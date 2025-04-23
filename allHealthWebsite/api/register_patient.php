<?php
header('Content-Type: application/json');
require_once '../config.php';

// Get the request method
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    // Get the JSON data from the request body
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($data['first_name']) || !isset($data['second_name']) || !isset($data['username']) || !isset($data['password'])) {
        http_response_code(400);
        echo json_encode(['error' => 'First name, second name, username, and password are required']);
        exit();
    }

    try {
        $pdo->beginTransaction();

        // Check if username already exists
        $stmt = $pdo->prepare("SELECT id FROM users WHERE username = ?");
        $stmt->execute([$data['username']]);
        if ($stmt->fetch()) {
            http_response_code(400);
            echo json_encode(['error' => 'Username already exists']);
            exit();
        }

        // Insert patient
        $stmt = $pdo->prepare("INSERT INTO patients (first_name, second_name) VALUES (?, ?)");
        $stmt->execute([$data['first_name'], $data['second_name']]);
        $patient_id = $pdo->lastInsertId();

        // Insert user
        $stmt = $pdo->prepare("INSERT INTO users (username, password, patient_id) VALUES (?, ?, ?)");
        $stmt->execute([$data['username'], $data['password'], $patient_id]);

        $pdo->commit();
        echo json_encode(['success' => true, 'message' => 'Patient registered successfully']);
    } catch (Exception $e) {
        $pdo->rollBack();
        http_response_code(500);
        echo json_encode(['error' => 'Database error: ' . $e->getMessage()]);
    }
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?> 