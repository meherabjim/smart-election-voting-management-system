package com.example.votingsystem3.DAO;

import com.example.votingsystem3.models.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class MessageDAO {

    // মেসেজ বা রেপ্লাই মেসেজ পাঠানোর ফাংশন
    public boolean sendMessage(int senderId, Integer receiverId, Integer pollingStationId, String message, Integer parentMessageId) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, polling_station_id, message, reply_to_message_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            if (receiverId != null)
                ps.setInt(2, receiverId);
            else
                ps.setNull(2, Types.INTEGER);

            if (pollingStationId != null)
                ps.setInt(3, pollingStationId);
            else
                ps.setNull(3, Types.INTEGER);

            ps.setString(4, message);

            if (parentMessageId != null)
                ps.setInt(5, parentMessageId);
            else
                ps.setNull(5, Types.INTEGER);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // পোলিং স্টেশন অ্যাডমিনের জন্য মেসেজ রিসিভ করার ফাংশন
    public ObservableList<Message> getMessagesForPollingStationAdmin(int adminId, int pollingStationId) {
        ObservableList<Message> list = FXCollections.observableArrayList();

        String sql = "SELECT m.id, m.sender_id, m.receiver_id, m.polling_station_id, m.message, m.timestamp, " +
                "su.email AS senderEmail, ru.email AS receiverEmail, m.reply_to_message_id " +
                "FROM messages m " +
                "JOIN users su ON m.sender_id = su.id " +
                "LEFT JOIN users ru ON m.receiver_id = ru.id " +
                "WHERE (m.sender_id = ? AND m.receiver_id = 1 AND m.polling_station_id = ?) " +
                "   OR (m.sender_id = 1 AND m.receiver_id = ? AND m.polling_station_id = ?) " +
                "   OR (m.sender_id = 1 AND m.receiver_id IS NULL AND m.polling_station_id IS NULL) " +
                "ORDER BY m.timestamp DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setInt(2, pollingStationId);
            ps.setInt(3, adminId);
            ps.setInt(4, pollingStationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer receiverId = rs.getObject("receiver_id") == null ? null : rs.getInt("receiver_id");
                    Integer pollId = rs.getObject("polling_station_id") == null ? null : rs.getInt("polling_station_id");
                    Integer replyToId = rs.getObject("reply_to_message_id") == null ? null : rs.getInt("reply_to_message_id");

                    Message msg = new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            receiverId,
                            pollId,
                            rs.getString("message"),
                            rs.getTimestamp("timestamp"),
                            rs.getString("senderEmail"),
                            rs.getString("receiverEmail"),
                            replyToId
                    );
                    list.add(msg);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // সুপার অ্যাডমিনের জন্য মেসেজ রিসিভ করার ফাংশন
    public ObservableList<Message> getMessagesForSuperAdmin(int superAdminId) {
        ObservableList<Message> list = FXCollections.observableArrayList();

        String sql = "SELECT m.id, m.sender_id, m.receiver_id, m.polling_station_id, m.message, m.timestamp, " +
                "su.email AS senderEmail, ru.email AS receiverEmail, m.reply_to_message_id " +
                "FROM messages m " +
                "JOIN users su ON m.sender_id = su.id " +
                "LEFT JOIN users ru ON m.receiver_id = ru.id " +
                "WHERE m.receiver_id = ? " +
                "   OR (m.receiver_id IS NULL AND m.sender_id = ?) " +
                "ORDER BY m.timestamp DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, superAdminId); // সাধারণত superadmin id: 1
            ps.setInt(2, superAdminId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer receiverId = rs.getObject("receiver_id") == null ? null : rs.getInt("receiver_id");
                    Integer pollId = rs.getObject("polling_station_id") == null ? null : rs.getInt("polling_station_id");
                    Integer replyToId = rs.getObject("reply_to_message_id") == null ? null : rs.getInt("reply_to_message_id");

                    Message msg = new Message(
                            rs.getInt("id"),
                            rs.getInt("sender_id"),
                            receiverId,
                            pollId,
                            rs.getString("message"),
                            rs.getTimestamp("timestamp"),
                            rs.getString("senderEmail"),
                            rs.getString("receiverEmail"),
                            replyToId
                    );
                    list.add(msg);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
