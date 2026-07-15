package com.example.votingsystem3.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static com.example.votingsystem3.util.DatabaseConnection.getConnection;

public class RegistrationController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label statusLabel;

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            statusLabel.setText("Please fill all fields!");
            System.err.println("DEBUG: Empty email or password field.");
            return;
        }

        System.out.println("DEBUG: Trying to register user with email: " + email);

        String sql = "INSERT INTO users(email, password, role) VALUES (?, ?, 'user')";

        try (Connection conn = getConnection()) {
            System.out.println("DEBUG: Database connection = " + conn);

            if (conn == null) {
                statusLabel.setText("Database connection failed.");
                System.err.println("DEBUG: Connection is null.");
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                stmt.setString(2, password);
                int rowsInserted = stmt.executeUpdate();

                System.out.println("DEBUG: Rows inserted = " + rowsInserted);

                if (rowsInserted > 0) {
                    statusLabel.setText("Registration Successful!");
                } else {
                    statusLabel.setText("Registration failed, no row inserted.");
                }
            }

        } catch (Exception e) {
            statusLabel.setText("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleBackToLogin() {
        try {
            URL fxmlLocation = getClass().getResource("/com/example/votingsystem3/login.fxml");
            if (fxmlLocation == null) {
                statusLabel.setText("Login page not found! Check your FXML file path.");
                System.err.println("ERROR: login.fxml not found in resources.");
                return;
            }
            Parent root = FXMLLoader.load(fxmlLocation);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Error loading login page.");
            e.printStackTrace();
        }
    }
}
