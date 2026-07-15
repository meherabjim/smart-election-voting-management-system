package com.example.votingsystem3.models;
public class PollingStation {
    private int id; private String name; private int upazilaId;
    public PollingStation(int id, String name, int upazilaId){this.id=id;this.name=name;this.upazilaId=upazilaId;}
    public int getId(){return id;} public String getName(){return name;}
    @Override public String toString(){return name;}
}
