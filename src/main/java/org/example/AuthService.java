package org.example;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public void register(String username, String password) {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashed);
            stmt.executeUpdate();
            System.out.println("User registered: " + username);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public String login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    return JwtUtil.generateToken(username);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public void accessProtectedResource(String token) {
        String username = JwtUtil.validateTokenAndGetUsername(token);
        if (username != null) {
            System.out.println("Access granted! Hello, " + username);
        } else {
            System.out.println("Access denied! Invalid or expired token.");
        }
    }
}
