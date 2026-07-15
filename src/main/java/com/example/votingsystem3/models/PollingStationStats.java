package com.example.votingsystem3.models;

import javafx.beans.property.*;

public class PollingStationStats {
    private final StringProperty stationName;
    private final IntegerProperty registeredCount;
    private final IntegerProperty voteCount;

    public PollingStationStats(String name, int registered, int voted) {
        this.stationName = new SimpleStringProperty(name);
        this.registeredCount = new SimpleIntegerProperty(registered);
        this.voteCount = new SimpleIntegerProperty(voted);
    }

    public StringProperty stationNameProperty() { return stationName; }
    public IntegerProperty registeredCountProperty() { return registeredCount; }
    public IntegerProperty voteCountProperty() { return voteCount; }
}
