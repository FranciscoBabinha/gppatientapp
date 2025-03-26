<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'db_connect.php';

try {
    $stmt = $pdo->query("SELECT * FROM payments WHERE patient_id = 1");
    $results = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    echo json_encode($results);
} catch(PDOException $e) {
    echo json_encode(['error' => $e->getMessage()]);
}
?>