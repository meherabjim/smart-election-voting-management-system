package com.example.votingsystem3.DAO;

import com.example.votingsystem3.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBUtil {

    private DBUtil() {
    }

    public static Connection getConnection()
            throws SQLException {

        return DatabaseConnection.getConnection();
    }
}
