package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InputDataDAO {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/HelloWorldDB?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";

    public boolean insertInputText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO user_input (input_text) VALUES (?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, text);
            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllEntries() {
        List<String> entries = new ArrayList<>();
        String sql = "SELECT input_text FROM user_input";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entries.add(rs.getString("input_text"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entries;
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure JDBC driver is registered
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }
}