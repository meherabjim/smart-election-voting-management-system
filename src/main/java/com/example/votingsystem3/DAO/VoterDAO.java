package com.example.votingsystem3.DAO;

import com.example.votingsystem3.models.Voter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VoterDAO {

    public boolean isRegisteredAnywhere(String nid) {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM voters " +
                        "WHERE nid_number = ?";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, nid);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        && resultSet.getInt(1) > 0;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean isRegisteredInStation(
            String nid,
            int stationId
    ) {

        String sql =
                "SELECT id " +
                        "FROM voters " +
                        "WHERE nid_number = ? " +
                        "AND polling_station_id = ?";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, nid);
            statement.setInt(2, stationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean hasVotedAnywhere(String nid) {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM voters " +
                        "WHERE nid_number = ? " +
                        "AND voted = TRUE";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, nid);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        && resultSet.getInt(1) > 0;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean registerNID(
            String nid,
            int stationId
    ) {

        if (
                nid == null
                        || nid.trim().isEmpty()
                        || stationId <= 0
        ) {
            return false;
        }

        String cleanNid = nid.trim();

        if (isRegisteredAnywhere(cleanNid)) {
            return false;
        }

        String sql =
                "INSERT INTO voters (" +
                        "nid_number, polling_station_id, " +
                        "registered_time, voted" +
                        ") VALUES (?, ?, CURRENT_TIMESTAMP, FALSE)";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, cleanNid);
            statement.setInt(2, stationId);

            return statement.executeUpdate() == 1;

        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public int getRegisteredCountByStation(int stationId) {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM voters " +
                        "WHERE polling_station_id = ?";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, stationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? resultSet.getInt(1)
                        : 0;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public int getVoteCountByStation(int stationId) {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM voters " +
                        "WHERE polling_station_id = ? " +
                        "AND voted = TRUE";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, stationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? resultSet.getInt(1)
                        : 0;
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public int getTotalVoteCount() {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM voters " +
                        "WHERE voted = TRUE";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet =
                        statement.executeQuery()
        ) {
            return resultSet.next()
                    ? resultSet.getInt(1)
                    : 0;

        } catch (SQLException exception) {
            exception.printStackTrace();
            return 0;
        }
    }

    public ObservableList<Voter> getVotersByPollingStation(
            int stationId
    ) {

        ObservableList<Voter> voters =
                FXCollections.observableArrayList();

        String sql =
                "SELECT id, nid_number, voted " +
                        "FROM voters " +
                        "WHERE polling_station_id = ? " +
                        "ORDER BY id";

        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, stationId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    voters.add(
                            new Voter(
                                    resultSet.getInt("id"),
                                    resultSet.getString("nid_number"),
                                    resultSet.getBoolean("voted")
                            )
                    );
                }
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return voters;
    }
}