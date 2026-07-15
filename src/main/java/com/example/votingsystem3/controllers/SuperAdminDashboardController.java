package com.example.votingsystem3.controllers;

import com.example.votingsystem3.DAO.MessageDAO;
import com.example.votingsystem3.DAO.SuperAdminDAO;
import com.example.votingsystem3.models.AdminAssignment;
import com.example.votingsystem3.models.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;

public class SuperAdminDashboardController {

    @FXML private ComboBox<String> divCombo;
    @FXML private ComboBox<String> disCombo;
    @FXML private ComboBox<String> upaCombo;
    @FXML private ComboBox<String> stationCombo;
    @FXML private TextField adminEmailField;
    @FXML private PasswordField adminPasswordField;
    @FXML private TableView<AdminAssignment> adminTable;
    @FXML private TableColumn<AdminAssignment, String> stationCol;
    @FXML private TableColumn<AdminAssignment, String> adminEmailCol;
    @FXML private Label statusLabel;

    @FXML private ListView<Message> messagesList;
    @FXML private TextField broadcastField;
    @FXML private Button broadcastButton;

    private final SuperAdminDAO dao = new SuperAdminDAO();
    private final MessageDAO messageDAO = new MessageDAO();

    @FXML
    public void initialize() {
        divCombo.setItems(dao.getDivisionNames());
        stationCol.setCellValueFactory(cell -> cell.getValue().stationNameProperty());
        adminEmailCol.setCellValueFactory(cell -> cell.getValue().adminEmailProperty());
        adminTable.setItems(FXCollections.observableArrayList(dao.getAdminAssignments()));

        messagesList.setCellFactory(listView -> new MessageListCell(dao, this));
        messagesList.setUserData(this);

        loadMessages();

        broadcastButton.setOnAction(e -> handleBroadcast());
    }

    @FXML private void handleDivSelect() {
        String division = divCombo.getValue();
        if (division != null) {
            disCombo.setItems(dao.getDistrictNames(division));
            disCombo.getSelectionModel().clearSelection();
            upaCombo.getItems().clear();
            stationCombo.getItems().clear();
        }
    }

    @FXML private void handleDisSelect() {
        String district = disCombo.getValue();
        if (district != null) {
            upaCombo.setItems(dao.getUpazilaNames(district));
            upaCombo.getSelectionModel().clearSelection();
            stationCombo.getItems().clear();
        }
    }

    @FXML private void handleUpaSelect() {
        String upazila = upaCombo.getValue();
        if (upazila != null) {
            stationCombo.setItems(dao.getPollingStationNames(upazila));
            stationCombo.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void handleAssignAdmin() {
        String email = adminEmailField.getText();
        String password = adminPasswordField.getText();
        String stationName = stationCombo.getValue();
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty() ||
                stationName == null || stationName.trim().isEmpty()) {
            statusLabel.setText("Please fill all fields!");
            return;
        }
        int stationId = dao.getStationIdByName(stationName);
        boolean ok = dao.assignAdminToStation(email, password, stationId);
        statusLabel.setText(ok ? "Admin assigned successfully!" : "Assignment failed!");
        adminTable.setItems(FXCollections.observableArrayList(dao.getAdminAssignments()));
        loadMessages();
    }

    private void loadMessages() {
        int superAdminId = getCurrentSuperAdminId();
        ObservableList<Message> messages = messageDAO.getMessagesForSuperAdmin(superAdminId);
        messagesList.setItems(messages);
        // Optionally: messagesList.refresh();
    }


    private int getCurrentSuperAdminId() {
        return 1; // Replace with actual session logic
    }

    @FXML
    private void handleBroadcast() {
        String messageToBroadcast = broadcastField.getText();
        if (messageToBroadcast == null || messageToBroadcast.trim().isEmpty()) {
            statusLabel.setText("Please enter a message to broadcast.");
            return;
        }
        int superAdminId = getCurrentSuperAdminId();
        boolean success = messageDAO.sendMessage(superAdminId, null, null, messageToBroadcast, null);

        if (success) {
            statusLabel.setText("Message broadcasted to all polling station admins.");
            broadcastField.clear();
            loadMessages();
        } else {
            statusLabel.setText("Failed to broadcast message.");
        }
    }

    public void sendReplyToPollingStation(Integer pollingStationId, String replyText, Integer parentMessageId) {
        int superAdminId = getCurrentSuperAdminId();
        int adminId = dao.getAdminIdByPollingStation(pollingStationId);
        if (adminId == -1) {
            statusLabel.setText("No admin assigned to this polling station.");
            return;
        }
        boolean success = messageDAO.sendMessage(superAdminId, adminId, pollingStationId, replyText, parentMessageId);
        if (success) {
            statusLabel.setText("Reply sent.");
            loadMessages();
        } else {
            statusLabel.setText("Reply failed.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            URL fxml = getClass().getResource("/com/example/votingsystem3/login.fxml");
            if (fxml == null) {
                statusLabel.setText("FXML not found!");
                return;
            }
            Parent root = FXMLLoader.load(fxml);
            Stage stage = (Stage) adminTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
        } catch (Exception e) {
            statusLabel.setText("Logout failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
