package com.example.votingsystem3.models;

import javafx.beans.property.*;

public class Voter {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty nid = new SimpleStringProperty();
    private final BooleanProperty voted = new SimpleBooleanProperty();

    public Voter(int id, String nid, boolean voted) {
        this.id.set(id);
        this.nid.set(nid);
        this.voted.set(voted);
    }

    public int getId() { return id.get(); }
    public String getNid() { return nid.get(); }
    public boolean isVoted() { return voted.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nidProperty() { return nid; }
    public BooleanProperty votedProperty() { return voted; }

    public void setId(int id) { this.id.set(id); }
    public void setNid(String nid) { this.nid.set(nid); }
    public void setVoted(boolean voted) { this.voted.set(voted); }
}
