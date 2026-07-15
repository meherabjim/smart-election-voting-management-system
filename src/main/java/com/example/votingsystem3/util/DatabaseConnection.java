package com.example.votingsystem3.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DATABASE_URL =
            "jdbc:mysql://127.0.0.1:3306/voting_system_db1"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC"
                    + "&characterEncoding=UTF-8";

    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "";

    private DatabaseConnection() {
    }

    public static Connection getConnection()
            throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException(
                    "MySQL JDBC Driver not found.",
                    exception
            );
        }

        return DriverManager.getConnection(
                DATABASE_URL,
                DATABASE_USER,
                DATABASE_PASSWORD
        );
    }

    public static boolean testConnection() {

        try (Connection connection = getConnection()) {

            if (
                    connection != null
                            && !connection.isClosed()
            ) {
                System.out.println(
                        "Database connected successfully: voting_system_db1"
                );

                return true;
            }

        } catch (SQLException exception) {
            System.err.println(
                    "Database connection failed: "
                            + exception.getMessage()
            );

            exception.printStackTrace();
        }

        return false;
    }
}