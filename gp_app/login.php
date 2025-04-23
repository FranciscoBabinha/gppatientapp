<?php
// Connect to database
$conn = new mysqli("localhost", "root", "", "gp_app");

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get POST data
$username = $_POST['username'];
$password = $_POST['password'];

// Query the database
$sql = "SELECT * FROM users WHERE username = ? AND password = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $username, $password);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    echo json_encode([
        'status' => 'success', 
        'user_id' => $user['id'],
        'patient_id' => $user['patient_id']
    ]);
} else {
    echo json_encode(['status' => 'failure']);
}

$stmt->close();
$conn->close();
?>
