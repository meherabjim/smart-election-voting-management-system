package com.example.votingsystem3.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
public class AdminAssignment {
    private final StringProperty stationName;
    private final StringProperty adminEmail;
    public AdminAssignment(String stationName, String adminEmail) {
        this.stationName = new SimpleStringProperty(stationName);
        this.adminEmail = new SimpleStringProperty(adminEmail);
    }
    public String getStationName() { return stationName.get(); }
    public StringProperty stationNameProperty() { return stationName; }
    public String getAdminEmail() { return adminEmail.get(); }
    public StringProperty adminEmailProperty() { return adminEmail; }
}
