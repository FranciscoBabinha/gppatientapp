<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$host = 'localhost';
$dbname = 'gp_app';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Get patient_id from request
    $patient_id = isset($_GET['patient_id']) ? $_GET['patient_id'] : null;
    
    if (!$patient_id) {
        echo json_encode(['error' => 'Patient ID is required']);
        exit;
    }
    
    $stmt = $pdo->prepare("SELECT DISTINCT allergy, severity FROM allergies WHERE patient_id = ?");
    $stmt->execute([$patient_id]);
    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode($result);
    
} catch(PDOException $e) {
    echo json_encode(['error' => 'Database error: ' . $e->getMessage()]);
}
?>