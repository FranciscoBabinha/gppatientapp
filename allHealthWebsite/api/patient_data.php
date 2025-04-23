<?php
header('Content-Type: application/json');
require_once '../config.php';

// Get the request method
$method = $_SERVER['REQUEST_METHOD'];

if ($method === 'POST') {
    // Get the JSON data from the request body
    $data = json_decode(file_get_contents('php://input'), true);
    
    if (!isset($data['username']) || !isset($data['password'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Username and password are required']);
        exit();
    }

    try {
        // Authenticate user
        $stmt = $pdo->prepare("SELECT u.*, p.first_name, p.second_name FROM users u 
                              LEFT JOIN patients p ON u.patient_id = p.id 
                              WHERE u.username = ?");
        $stmt->execute([$data['username']]);
        $user = $stmt->fetch();

        if ($user && $data['password'] === $user['password']) {
            if ($user['patient_id'] === null) {
                http_response_code(403);
                echo json_encode(['error' => 'Admin access not allowed through API']);
                exit();
            }

            // Fetch patient information
            $stmt = $pdo->prepare("SELECT * FROM patients WHERE id = ?");
            $stmt->execute([$user['patient_id']]);
            $patient = $stmt->fetch();

            // Fetch diagnoses
            $stmt = $pdo->prepare("SELECT * FROM diagnoses WHERE patient_id = ?");
            $stmt->execute([$user['patient_id']]);
            $diagnoses = $stmt->fetchAll();

            // Fetch allergies
            $stmt = $pdo->prepare("SELECT * FROM allergies WHERE patient_id = ?");
            $stmt->execute([$user['patient_id']]);
            $allergies = $stmt->fetchAll();

            // Fetch medications
            $stmt = $pdo->prepare("SELECT * FROM medications WHERE patient_id = ?");
            $stmt->execute([$user['patient_id']]);
            $medications = $stmt->fetchAll();

            // Fetch results
            $stmt = $pdo->prepare("SELECT * FROM results WHERE patient_id = ?");
            $stmt->execute([$user['patient_id']]);
            $results = $stmt->fetchAll();

            // Fetch vaccines
            $stmt = $pdo->prepare("SELECT * FROM vaccines WHERE patient_id = ?");
            $stmt->execute([$user['patient_id']]);
            $vaccines = $stmt->fetchAll();

            // Return all patient data
            echo json_encode([
                'patient' => $patient,
                'diagnoses' => $diagnoses,
                'allergies' => $allergies,
                'medications' => $medications,
                'results' => $results,
                'vaccines' => $vaccines
            ]);
        } else {
            http_response_code(401);
            echo json_encode(['error' => 'Invalid username or password']);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['error' => 'Database error: ' . $e->getMessage()]);
    }
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?> 