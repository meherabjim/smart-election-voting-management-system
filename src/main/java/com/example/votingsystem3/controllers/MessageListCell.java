package com.example.votingsystem3.controllers;

import com.example.votingsystem3.DAO.SuperAdminDAO;
import com.example.votingsystem3.models.Message;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MessageListCell extends ListCell<Message> {
    private final SuperAdminDAO dao;
    private final SuperAdminDashboardController controller;

    private final Label stationLabel = new Label();
    private final Label messageLabel = new Label();
    private final TextField replyField = new TextField();
    private final Button sendReplyButton = new Button("Reply");

    public MessageListCell(SuperAdminDAO dao, SuperAdminDashboardController controller) {
        this.dao = dao;
        this.controller = controller;

        replyField.setPromptText("Type reply here");
        replyField.setPrefWidth(200);

        sendReplyButton.setOnAction(event -> {
            Message message = getItem();
            if (message != null) {
                String replyText = replyField.getText();
                if (replyText == null || replyText.trim().isEmpty()) return;
                controller.sendReplyToPollingStation(message.getPollingStationId(), replyText, message.getId());
                replyField.clear();
            }
        });
    }

    @Override
    protected void updateItem(Message msg, boolean empty) {
        super.updateItem(msg, empty);
        if (empty || msg == null) {
            setGraphic(null);
            return;
        }

        HBox cellLayout = new HBox(10);
        VBox msgBox = new VBox(6);

        if (msg.getReceiverId() == null && msg.getSenderId() == 1) {
            stationLabel.setText("All polling station SMS");
            messageLabel.setText(msg.getMessage());
            msgBox.getChildren().setAll(stationLabel, messageLabel);
            cellLayout.getChildren().setAll(msgBox);
        } else if (msg.getSenderId() == 1 && msg.getReceiverId() != null) {
            String stationName = dao.getPollingStationName(msg.getPollingStationId());
            stationLabel.setText("Polling Station: " + (stationName != null ? stationName : "Unknown"));
            messageLabel.setText(msg.getMessage());
            msgBox.getChildren().setAll(stationLabel, messageLabel);
            cellLayout.getChildren().setAll(msgBox); // No reply field on super admin sent messages to individual
        } else if (msg.getSenderId() != 1 && msg.getReceiverId() != null && msg.getReceiverId() == 1) {
            String stationName = dao.getPollingStationName(msg.getPollingStationId());
            stationLabel.setText("Polling Station: " + (stationName != null ? stationName : "Unknown"));
            messageLabel.setText(msg.getMessage());
            msgBox.getChildren().setAll(stationLabel, messageLabel);
            cellLayout.getChildren().setAll(msgBox, replyField, sendReplyButton); // Reply field visible only when polling station admin sent message to super admin
        } else {
            messageLabel.setText(msg.getMessage());
            msgBox.getChildren().setAll(messageLabel);
            cellLayout.getChildren().setAll(msgBox);
        }
        setGraphic(cellLayout);
    }
}
