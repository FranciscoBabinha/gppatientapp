<?php
// Database connection settings
$servername = "localhost";
$username = "root"; // Default XAMPP username
$password = ""; // Default XAMPP password
$dbname = "gp_app";

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// SQL query to fetch patient data
$sql = "SELECT * FROM patients";
$result = $conn->query($sql);

// Display results
if ($result->num_rows > 0) {
    echo "<h1>Patient List</h1>";
    echo "<table border='1'><tr><th>ID</th><th>First Name</th><th>Second Name</th></tr>";
    while ($row = $result->fetch_assoc()) {
        echo "<tr><td>" . $row["id"] . "</td><td>" . $row["first_name"] . "</td><td>" . $row["second_name"] . "</td></tr>";
    }
    echo "</table>";
} else {
    echo "No patients found.";
}

// Close connection
$conn->close();
?>
