<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php';

try {
    // Get patient_id from request
    $patient_id = isset($_GET['patient_id']) ? $_GET['patient_id'] : null;
    
    if (!$patient_id) {
        echo json_encode(['error' => 'Patient ID is required']);
        exit;
    }
    
    $stmt = $pdo->prepare("SELECT * FROM payments WHERE patient_id = ?");
    $stmt->execute([$patient_id]);
    $results = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode($results);
} catch(PDOException $e) {
    echo json_encode(['error' => $e->getMessage()]);
}
?>