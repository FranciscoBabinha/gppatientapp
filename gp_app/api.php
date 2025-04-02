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
    
    // Get the username from the request
    $username = isset($_GET['username']) ? $_GET['username'] : '';
    
    if (empty($username)) {
        echo json_encode(['error' => 'Username is required']);
        exit;
    }
    
    // Join users and patients tables to get the correct patient name
    $stmt = $pdo->prepare("SELECT p.first_name, p.second_name 
                          FROM patients p 
                          JOIN users u ON p.id = u.patient_id 
                          WHERE u.username = ?");
    $stmt->execute([$username]);
    $result = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (empty($result)) {
        echo json_encode(['error' => 'Patient not found']);
        exit;
    }
    
    echo json_encode($result);
    
} catch(PDOException $e) {
    echo json_encode(['error' => 'Database error: ' . $e->getMessage()]);
}
?>