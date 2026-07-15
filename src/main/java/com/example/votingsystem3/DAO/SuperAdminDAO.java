package com.example.votingsystem3.DAO;

import com.example.votingsystem3.models.AdminAssignment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class SuperAdminDAO {

    public boolean assignAdminToStation(String email, String password, int stationId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            int adminId;

            try (PreparedStatement findUser = conn.prepareStatement("SELECT id FROM users WHERE email=?")) {
                findUser.setString(1, email);
                try (ResultSet rs = findUser.executeQuery()) {
                    if (rs.next()) {
                        adminId = rs.getInt("id");
                        try (PreparedStatement updatePassword = conn.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
                            updatePassword.setString(1, password);
                            updatePassword.setInt(2, adminId);
                            updatePassword.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement insertUser = conn.prepareStatement(
                                "INSERT INTO users (email, password, role) VALUES (?, ?, 'admin')",
                                Statement.RETURN_GENERATED_KEYS)) {
                            insertUser.setString(1, email);
                            insertUser.setString(2, password);
                            insertUser.executeUpdate();
                            try (ResultSet keys = insertUser.getGeneratedKeys()) {
                                if (keys.next()) adminId = keys.getInt(1);
                                else {
                                    conn.rollback();
                                    return false;
                                }
                            }
                        }
                    }
                }
            }

            try (PreparedStatement deleteOldMapping = conn.prepareStatement("DELETE FROM admin_polling_station WHERE admin_id=? OR polling_station_id=?")) {
                deleteOldMapping.setInt(1, adminId);
                deleteOldMapping.setInt(2, stationId);
                deleteOldMapping.executeUpdate();
            }
            try (PreparedStatement insertMapping = conn.prepareStatement("INSERT INTO admin_polling_station (admin_id, polling_station_id) VALUES (?, ?)")) {
                insertMapping.setInt(1, adminId);
                insertMapping.setInt(2, stationId);
                insertMapping.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }

    public ObservableList<String> getDivisionNames() {
        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT name FROM division";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(rs.getString("name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ObservableList<String> getDistrictNames(String divisionName) {
        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT d.name FROM district d JOIN division v ON d.division_id = v.id WHERE v.name = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, divisionName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ObservableList<String> getUpazilaNames(String districtName) {
        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT u.name FROM upazila u JOIN district d ON u.district_id = d.id WHERE d.name = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, districtName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("name"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ObservableList<String> getPollingStationNames(String upazilaName) {
        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT ps.name FROM polling_station ps JOIN upazila u ON ps.upazila_id = u.id WHERE u.name = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, upazilaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getStationIdByName(String name) {
        String sql = "SELECT id FROM polling_station WHERE name=?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public ObservableList<AdminAssignment> getAdminAssignments() {
        ObservableList<AdminAssignment> list = FXCollections.observableArrayList();
        String sql = "SELECT ps.name AS stationName, u.email AS adminEmail FROM polling_station ps " +
                "LEFT JOIN admin_polling_station aps ON ps.id = aps.polling_station_id " +
                "LEFT JOIN users u ON aps.admin_id = u.id";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String stationName = rs.getString("stationName");
                String adminEmail = rs.getString("adminEmail") != null ? rs.getString("adminEmail") : "No Admin";
                list.add(new AdminAssignment(stationName, adminEmail));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getAdminIdByPollingStation(int pollingStationId) {
        String sql = "SELECT admin_id FROM admin_polling_station WHERE polling_station_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pollingStationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("admin_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getPollingStationName(int stationId) {
        String sql = "SELECT name FROM polling_station WHERE id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
