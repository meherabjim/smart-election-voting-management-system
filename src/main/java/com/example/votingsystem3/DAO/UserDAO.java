package com.example.votingsystem3.DAO;

import com.example.votingsystem3.models.User;
import java.sql.*;

public class UserDAO {
    public boolean registerUser(String email, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users (email, password, role) VALUES (?, ?, 'user')"
            );
            ps.setString(1, email);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public User getUser(String email, String password) {
        try (Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, email, role FROM users WHERE email=? AND password=?"
            );
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("email"), rs.getString("role"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
