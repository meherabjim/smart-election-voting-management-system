package com.example.votingsystem3.controllers;

import com.example.votingsystem3.DAO.LocationDAO;
import com.example.votingsystem3.DAO.VoteDAO;
import com.example.votingsystem3.DAO.VoterDAO;
import com.example.votingsystem3.models.Candidate;
import com.example.votingsystem3.models.District;
import com.example.votingsystem3.models.Division;
import com.example.votingsystem3.models.PollingStation;
import com.example.votingsystem3.models.Upazila;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

public class UserDashboardController {

    @FXML
    private ComboBox<Division> divCombo;

    @FXML
    private ComboBox<District> disCombo;

    @FXML
    private ComboBox<Upazila> upaCombo;

    @FXML
    private ComboBox<PollingStation> stationCombo;

    @FXML
    private Label bangladeshLiveCountLabel;

    @FXML
    private Label selectedStationLiveCountLabel;

    @FXML
    private Label nationalProgressLabel;

    @FXML
    private Label nationalValidLabel;

    @FXML
    private Label nationalInvalidLabel;

    @FXML
    private Label stationResultStatusLabel;

    @FXML
    private Label stationValidLabel;

    @FXML
    private Label stationInvalidLabel;

    @FXML
    private TableView<Candidate> stationTable;

    @FXML
    private TableColumn<Candidate, String> candidateCol1;

    @FXML
    private TableColumn<Candidate, Number> votesCol1;

    @FXML
    private TableView<Candidate> bdTable;

    @FXML
    private TableColumn<Candidate, String> candidateCol2;

    @FXML
    private TableColumn<Candidate, Number> votesCol2;

    private final LocationDAO locationDAO =
            new LocationDAO();

    private final VoteDAO voteDAO =
            new VoteDAO();

    private final VoterDAO voterDAO =
            new VoterDAO();

    private Timeline liveTimeline;

    @FXML
    public void initialize() {

        /*
         * Selected polling station table columns.
         */
        candidateCol1.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        votesCol1.setCellValueFactory(
                new PropertyValueFactory<>("voteCount")
        );

        /*
         * Bangladesh result table columns.
         */
        candidateCol2.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        votesCol2.setCellValueFactory(
                new PropertyValueFactory<>("voteCount")
        );

        /*
         * Table resize policy Controller থেকে দেওয়া হয়েছে।
         * FXML-এর columnResizePolicy error আর হবে না।
         */
        bdTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        stationTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        /*
         * Initial table states.
         */
        bdTable.getItems().clear();
        stationTable.getItems().clear();

        /*
         * Load all divisions.
         */
        divCombo.setItems(
                FXCollections.observableArrayList(
                        locationDAO.getAllDivisions()
                )
        );

        /*
         * Cascading ComboBox initial state.
         */
        disCombo.setDisable(true);
        upaCombo.setDisable(true);
        stationCombo.setDisable(true);

        /*
         * Cascading selection handlers.
         */
        divCombo.setOnAction(
                event -> handleDivisionSelect()
        );

        disCombo.setOnAction(
                event -> handleDistrictSelect()
        );

        upaCombo.setOnAction(
                event -> handleUpazilaSelect()
        );

        stationCombo.setOnAction(
                event -> handleStationSelect()
        );

        /*
         * Initial dashboard refresh.
         */
        refreshUserDashboard();

        /*
         * প্রতি ১ সেকেন্ডে live data refresh হবে।
         */
        liveTimeline = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        event -> refreshUserDashboard()
                )
        );

        liveTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        liveTimeline.play();
    }

    private void refreshUserDashboard() {

        /*
         * Bangladesh live vote cast.
         */
        int bangladeshLiveVotes =
                voterDAO.getTotalVoteCount();

        bangladeshLiveCountLabel.setText(
                "Bangladesh Live Votes Cast: "
                        + bangladeshLiveVotes
        );

        /*
         * Bangladesh submitted final results.
         */
        loadNationalSubmittedResults();

        /*
         * Selected polling station information.
         */
        PollingStation selectedStation =
                stationCombo.getValue();

        if (selectedStation == null) {

            selectedStationLiveCountLabel.setText(
                    "Select a polling station."
            );

            return;
        }

        int stationLiveVotes =
                voterDAO.getVoteCountByStation(
                        selectedStation.getId()
                );

        selectedStationLiveCountLabel.setText(
                selectedStation.getName()
                        + " — Live Votes Cast: "
                        + stationLiveVotes
        );

        loadSelectedStationResult(
                selectedStation
        );
    }

    private void loadNationalSubmittedResults() {

        bdTable.setItems(
                FXCollections.observableArrayList(
                        voteDAO
                                .getCandidatesWithSubmittedTotalVotes()
                )
        );

        int submittedStations =
                voteDAO.getSubmittedStationCount();

        int totalStations =
                voteDAO.getTotalStationCount();

        int validVotes =
                voteDAO.getNationalValidVoteCount();

        int invalidVotes =
                voteDAO.getNationalInvalidVoteCount();

        nationalProgressLabel.setText(
                "Final results submitted: "
                        + submittedStations
                        + " / "
                        + totalStations
                        + " polling stations"
        );

        nationalValidLabel.setText(
                "Valid Votes: "
                        + validVotes
        );

        nationalInvalidLabel.setText(
                "Invalid Votes: "
                        + invalidVotes
        );
    }

    private void loadSelectedStationResult(
            PollingStation station
    ) {

        boolean resultSubmitted =
                voteDAO.isStationResultSubmitted(
                        station.getId()
                );

        if (!resultSubmitted) {

            stationTable.getItems().clear();

            stationResultStatusLabel.setText(
                    "Final result has not been submitted by this polling station."
            );

            stationValidLabel.setText(
                    "Valid Votes: 0"
            );

            stationInvalidLabel.setText(
                    "Invalid Votes: 0"
            );

            return;
        }

        stationTable.setItems(
                FXCollections.observableArrayList(
                        voteDAO.getCandidatesWithVotes(
                                station.getId()
                        )
                )
        );

        int validVotes =
                voteDAO.getStationValidVoteCount(
                        station.getId()
                );

        int invalidVotes =
                voteDAO.getStationInvalidVoteCount(
                        station.getId()
                );

        stationResultStatusLabel.setText(
                "Final result submitted."
        );

        stationValidLabel.setText(
                "Valid Votes: "
                        + validVotes
        );

        stationInvalidLabel.setText(
                "Invalid Votes: "
                        + invalidVotes
        );
    }

    private void handleDivisionSelect() {

        Division selectedDivision =
                divCombo.getValue();

        disCombo.getItems().clear();
        upaCombo.getItems().clear();
        stationCombo.getItems().clear();

        disCombo.getSelectionModel()
                .clearSelection();

        upaCombo.getSelectionModel()
                .clearSelection();

        stationCombo.getSelectionModel()
                .clearSelection();

        upaCombo.setDisable(true);
        stationCombo.setDisable(true);

        clearSelectedStationInformation();

        if (selectedDivision == null) {

            disCombo.setDisable(true);
            return;
        }

        var districts =
                locationDAO.getDistrictsByDivision(
                        selectedDivision.getId()
                );

        disCombo.setItems(
                FXCollections.observableArrayList(
                        districts
                )
        );

        disCombo.setDisable(
                districts.isEmpty()
        );
    }

    private void handleDistrictSelect() {

        District selectedDistrict =
                disCombo.getValue();

        upaCombo.getItems().clear();
        stationCombo.getItems().clear();

        upaCombo.getSelectionModel()
                .clearSelection();

        stationCombo.getSelectionModel()
                .clearSelection();

        stationCombo.setDisable(true);

        clearSelectedStationInformation();

        if (selectedDistrict == null) {

            upaCombo.setDisable(true);
            return;
        }

        var upazilas =
                locationDAO.getUpazilasByDistrict(
                        selectedDistrict.getId()
                );

        upaCombo.setItems(
                FXCollections.observableArrayList(
                        upazilas
                )
        );

        upaCombo.setDisable(
                upazilas.isEmpty()
        );
    }

    private void handleUpazilaSelect() {

        Upazila selectedUpazila =
                upaCombo.getValue();

        stationCombo.getItems().clear();

        stationCombo.getSelectionModel()
                .clearSelection();

        clearSelectedStationInformation();

        if (selectedUpazila == null) {

            stationCombo.setDisable(true);
            return;
        }

        var pollingStations =
                locationDAO.getStationsByUpazila(
                        selectedUpazila.getId()
                );

        stationCombo.setItems(
                FXCollections.observableArrayList(
                        pollingStations
                )
        );

        stationCombo.setDisable(
                pollingStations.isEmpty()
        );
    }

    private void handleStationSelect() {

        PollingStation selectedStation =
                stationCombo.getValue();

        if (selectedStation == null) {

            clearSelectedStationInformation();
            return;
        }

        refreshUserDashboard();
    }

    private void clearSelectedStationInformation() {

        selectedStationLiveCountLabel.setText(
                "Select a polling station."
        );

        stationResultStatusLabel.setText(
                "Select a polling station to see its result status."
        );

        stationValidLabel.setText(
                "Valid Votes: 0"
        );

        stationInvalidLabel.setText(
                "Invalid Votes: 0"
        );

        stationTable.getItems().clear();
    }

    private void stopLiveTimeline() {

        if (liveTimeline != null) {
            liveTimeline.stop();
        }
    }

    @FXML
    private void handleLogout(
            javafx.event.ActionEvent event
    ) {

        stopLiveTimeline();

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/votingsystem3/login.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Stage stage =
                    (Stage) (
                            (Button) event.getSource()
                    ).getScene().getWindow();

            stage.setScene(
                    new Scene(root)
            );

            stage.setTitle("Login");
            stage.show();

        } catch (Exception exception) {

            exception.printStackTrace();
        }
    }
}