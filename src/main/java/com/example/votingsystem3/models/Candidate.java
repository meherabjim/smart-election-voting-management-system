package com.example.votingsystem3.models;

import javafx.beans.property.*;

public class Candidate {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty voteCount = new SimpleIntegerProperty();
    private final IntegerProperty updatedVote = new SimpleIntegerProperty();

    public Candidate(int id, String name, int voteCount) {
        this.id.set(id);
        this.name.set(name);
        this.voteCount.set(voteCount);
        this.updatedVote.set(voteCount);
    }

    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public int getVoteCount() { return voteCount.get(); }
    public int getUpdatedVote() { return updatedVote.get(); }

    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty voteCountProperty() { return voteCount; }
    public IntegerProperty updatedVoteProperty() { return updatedVote; }

    public void setId(int id) { this.id.set(id); }
    public void setName(String name) { this.name.set(name); }
    public void setVoteCount(int voteCount) { this.voteCount.set(voteCount); }
    public void setUpdatedVote(int value) { updatedVote.set(value); }
}
