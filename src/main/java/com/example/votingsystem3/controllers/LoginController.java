package com.example.votingsystem3.controllers;

import com.example.votingsystem3.AdminSession;
import com.example.votingsystem3.DAO.AdminDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final AdminDAO adminDAO = new AdminDAO();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        int adminId = adminDAO.getAdminIdByEmailPassword(email, password);
        if (adminId == -1) {
            statusLabel.setText("Invalid email or password");
            return;
        }

        String role = adminDAO.getRoleByAdminId(adminId);

        try {
            FXMLLoader loader;
            Parent root;

            if ("superadmin".equalsIgnoreCase(role)) {
                AdminSession.setCurrentAdminId(adminId); // <-- এখানে session set করো
                loader = new FXMLLoader(getClass().getResource("/com/example/votingsystem3/superadmin_dashboard.fxml"));
                root = loader.load();
            } else if ("admin".equalsIgnoreCase(role)) {
                int stationId = adminDAO.getAssignedPollingStationId(adminId);
                String stationName = adminDAO.getPollingStationName(stationId);
                if (stationId == -1 || stationName == null) {
                    statusLabel.setText("Polling station not assigned to this admin");
                    return;
                }
                AdminSession.setCurrentAdminId(adminId); // <-- এখানে session set করো
                loader = new FXMLLoader(getClass().getResource("/com/example/votingsystem3/admin_dashboard.fxml"));
                root = loader.load();

                AdminDashboardController controller = loader.getController();
                controller.setPollingStation(stationId, stationName);
            } else {
                AdminSession.setCurrentAdminId(adminId); // <-- User এর জন্যও session set করো
                loader = new FXMLLoader(getClass().getResource("/com/example/votingsystem3/user_dashboard.fxml"));
                root = loader.load();
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();

        } catch (Exception e) {
            statusLabel.setText("Login failed.");
            e.printStackTrace();
        }
    }


    @FXML
    private void handleRegister() {
        // Registration logic or navigation to registration screen
        System.out.println("Register button clicked");
        // For example:
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/votingsystem3/registration.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Registration");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Cannot open registration screen.");
        }
    }
}
