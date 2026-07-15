package com.example.votingsystem3.controllers;

import com.example.votingsystem3.DAO.MessageDAO;
import com.example.votingsystem3.DAO.VoteDAO;
import com.example.votingsystem3.DAO.VoterDAO;
import com.example.votingsystem3.models.Candidate;
import com.example.votingsystem3.models.Message;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AdminDashboardController {

    @FXML
    private Label stationNameLabel;

    @FXML
    private Label pollingStatusLabel;

    @FXML
    private TableView<Candidate> resultTable;

    @FXML
    private TableColumn<Candidate, String>
            candidateNameCol;

    @FXML
    private TableColumn<Candidate, Integer>
            voteCountCol;

    @FXML
    private TableColumn<Candidate, Integer>
            updateVoteCol;

    @FXML
    private Button updateVotesButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField nidRegisterField;

    @FXML
    private Button registerButton;

    @FXML
    private Label registerStatusLabel;

    @FXML
    private Label registeredCountLabel;

    @FXML
    private TextField nidVoteField;

    @FXML
    private Button voteButton;

    @FXML
    private Label voteStatusLabel;

    @FXML
    private Label votedCountLabel;

    @FXML
    private ListView<Message> messagesList;

    @FXML
    private TextField replyField;

    @FXML
    private Button sendButton;

    @FXML
    private Label messageStatusLabel;

    @FXML
    private Button logoutButton;

    private final VoteDAO voteDAO =
            new VoteDAO();

    private final VoterDAO voterDAO =
            new VoterDAO();

    private final MessageDAO messageDAO =
            new MessageDAO();

    private int pollingStationId;
    private Timeline liveTimeline;
    private Boolean lastSubmittedState;

    @FXML
    public void initialize() {

        candidateNameCol.setCellValueFactory(
                cell ->
                        cell.getValue().nameProperty()
        );

        voteCountCol.setCellValueFactory(
                cell ->
                        cell.getValue()
                                .voteCountProperty()
                                .asObject()
        );

        updateVoteCol.setCellValueFactory(
                cell ->
                        cell.getValue()
                                .updatedVoteProperty()
                                .asObject()
        );

        updateVoteCol.setCellFactory(
                TextFieldTableCell.forTableColumn(
                        new IntegerStringConverter()
                )
        );

        updateVoteCol.setOnEditCommit(event -> {

            Candidate candidate =
                    event.getRowValue();

            Integer newValue =
                    event.getNewValue();

            if (
                    newValue == null
                            || newValue < 0
            ) {
                candidate.setUpdatedVote(
                        candidate.getVoteCount()
                );

                resultTable.refresh();

                statusLabel.setText(
                        "Vote count cannot be negative."
                );

                return;
            }

            candidate.setUpdatedVote(newValue);
        });

        resultTable.setEditable(true);

        registerButton.setOnAction(
                event -> registerNID()
        );

        voteButton.setOnAction(
                event -> castVote()
        );

        sendButton.setOnAction(
                event -> sendMessage()
        );

        logoutButton.setOnAction(
                event -> handleLogout()
        );

        messagesList.setCellFactory(
                listView ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    Message message,
                                    boolean empty
                            ) {
                                super.updateItem(
                                        message,
                                        empty
                                );

                                if (
                                        empty
                                                || message == null
                                ) {
                                    setText(null);
                                    return;
                                }

                                String sender =
                                        message.getSenderId() == 1
                                                ? "Super Admin: "
                                                : "You: ";

                                String timestamp =
                                        message.getTimestamp() == null
                                                ? ""
                                                : " ["
                                                + message.getTimestamp()
                                                + "]";

                                setText(
                                        sender
                                                + message.getMessage()
                                                + timestamp
                                );
                            }
                        }
        );

        /*
         * প্রতি ১ সেকেন্ডে database থেকে
         * live count automatic update।
         */
        liveTimeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event ->
                                refreshAdminDashboard()
                )
        );

        liveTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        liveTimeline.play();
    }

    public void setPollingStation(
            int pollingStationId,
            String stationName
    ) {

        this.pollingStationId =
                pollingStationId;

        this.lastSubmittedState =
                null;

        stationNameLabel.setText(
                "Polling Station: "
                        + stationName
        );

        loadCandidates();
        loadMessages();
        refreshAdminDashboard();
    }

    private void refreshAdminDashboard() {

        if (pollingStationId <= 0) {
            return;
        }

        int registeredCount =
                voterDAO.getRegisteredCountByStation(
                        pollingStationId
                );

        int liveVoteCount =
                voterDAO.getVoteCountByStation(
                        pollingStationId
                );

        registeredCountLabel.setText(
                "Registered: "
                        + registeredCount
        );

        votedCountLabel.setText(
                "Live Votes Cast: "
                        + liveVoteCount
        );

        boolean submitted =
                voteDAO.isStationResultSubmitted(
                        pollingStationId
                );

        registerButton.setDisable(submitted);
        nidRegisterField.setDisable(submitted);

        voteButton.setDisable(submitted);
        nidVoteField.setDisable(submitted);

        /*
         * Result correction করার জন্য
         * Submit button বন্ধ করা হয়নি।
         */
        updateVotesButton.setDisable(false);

        if (
                lastSubmittedState == null
                        || submitted
                        != lastSubmittedState
        ) {
            if (submitted) {
                pollingStatusLabel.setText(
                        "Polling Closed — Final Result Submitted"
                );

                pollingStatusLabel.setStyle(
                        "-fx-text-fill: #c62828; " +
                                "-fx-font-weight: bold;"
                );

            } else {
                pollingStatusLabel.setText(
                        "Polling Open — Live Count Updating Automatically"
                );

                pollingStatusLabel.setStyle(
                        "-fx-text-fill: #2e7d32; " +
                                "-fx-font-weight: bold;"
                );
            }

            lastSubmittedState = submitted;
        }
    }

    private void registerNID() {

        if (pollingStationId <= 0) {
            registerStatusLabel.setText(
                    "Polling station is not assigned."
            );
            return;
        }

        if (
                voteDAO.isStationResultSubmitted(
                        pollingStationId
                )
        ) {
            registerStatusLabel.setText(
                    "Polling is already closed."
            );
            return;
        }

        String nid =
                nidRegisterField.getText();

        if (
                nid == null
                        || nid.trim().isEmpty()
        ) {
            registerStatusLabel.setText(
                    "Please enter NID."
            );
            return;
        }

        boolean registered =
                voterDAO.registerNID(
                        nid.trim(),
                        pollingStationId
                );

        if (registered) {
            registerStatusLabel.setText(
                    "NID registered successfully."
            );

            nidRegisterField.clear();

        } else {
            registerStatusLabel.setText(
                    "NID is already registered."
            );
        }

        refreshAdminDashboard();
    }

    private void castVote() {

        if (pollingStationId <= 0) {
            voteStatusLabel.setText(
                    "Polling station is not assigned."
            );
            return;
        }

        String nid =
                nidVoteField.getText();

        if (
                nid == null
                        || nid.trim().isEmpty()
        ) {
            voteStatusLabel.setText(
                    "Please enter NID."
            );
            return;
        }

        VoteDAO.VoteResult result =
                voteDAO.attemptVoteByNid(
                        nid.trim(),
                        pollingStationId
                );

        voteStatusLabel.setText(
                result.message
        );

        if (result.success) {
            nidVoteField.clear();
        }

        /*
         * Vote হওয়ার সঙ্গে সঙ্গে count update।
         */
        refreshAdminDashboard();
    }

    private void loadCandidates() {

        if (pollingStationId <= 0) {
            return;
        }

        ObservableList<Candidate> candidates =
                FXCollections.observableArrayList(
                        voteDAO.getCandidatesWithVotes(
                                pollingStationId
                        )
                );

        candidates.forEach(candidate ->
                candidate.setUpdatedVote(
                        candidate.getVoteCount()
                )
        );

        resultTable.setItems(candidates);
    }

    @FXML
    private void handleUpdateVotes() {

        if (pollingStationId <= 0) {
            statusLabel.setText(
                    "Polling station is not assigned."
            );
            return;
        }

        if (resultTable.getItems().isEmpty()) {
            statusLabel.setText(
                    "No candidates found."
            );
            return;
        }

        Map<Integer, Integer> candidateVotes =
                new LinkedHashMap<>();

        for (
                Candidate candidate
                : resultTable.getItems()
        ) {
            int voteCount =
                    candidate.getUpdatedVote();

            if (voteCount < 0) {
                statusLabel.setText(
                        "Vote count cannot be negative."
                );
                return;
            }

            candidateVotes.put(
                    candidate.getId(),
                    voteCount
            );
        }

        VoteDAO.SubmissionResult result =
                voteDAO.submitFinalResult(
                        pollingStationId,
                        candidateVotes
                );

        if (result.success) {
            statusLabel.setText(
                    result.message
                            + " Valid: "
                            + result.validVotes
                            + ", Invalid: "
                            + result.invalidVotes
            );

            lastSubmittedState = null;
            loadCandidates();
            refreshAdminDashboard();

        } else {
            statusLabel.setText(
                    result.message
            );
        }
    }

    private void sendMessage() {

        String messageText =
                replyField.getText();

        if (
                messageText == null
                        || messageText.trim().isEmpty()
        ) {
            messageStatusLabel.setText(
                    "Please type a message."
            );
            return;
        }

        boolean sent =
                messageDAO.sendMessage(
                        pollingStationId,
                        1,
                        pollingStationId,
                        messageText.trim(),
                        null
                );

        if (sent) {
            messageStatusLabel.setText(
                    "Message sent."
            );

            replyField.clear();
            loadMessages();

        } else {
            messageStatusLabel.setText(
                    "Message could not be sent."
            );
        }
    }

    private void loadMessages() {

        if (pollingStationId <= 0) {
            return;
        }

        ObservableList<Message> messages =
                messageDAO
                        .getMessagesForPollingStationAdmin(
                                pollingStationId,
                                pollingStationId
                        );

        Platform.runLater(
                () ->
                        messagesList.setItems(
                                messages
                        )
        );
    }

    private void stopLiveTimeline() {

        if (liveTimeline != null) {
            liveTimeline.stop();
        }
    }

    @FXML
    private void handleLogout() {

        stopLiveTimeline();

        try {
            Parent root =
                    FXMLLoader.load(
                            Objects.requireNonNull(
                                    getClass().getResource(
                                            "/com/example/votingsystem3/login.fxml"
                                    )
                            )
                    );

            Stage stage =
                    (Stage) logoutButton
                            .getScene()
                            .getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception exception) {
            statusLabel.setText(
                    "Logout failed."
            );

            exception.printStackTrace();
        }
    }
}