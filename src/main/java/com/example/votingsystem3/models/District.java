package com.example.votingsystem3.models;
public class District {
    private final int id; private final String name;

    public District(int id, String name, int divisionId) { this.id=id; this.name=name;
    }
    public int getId(){return id;} public String getName(){return name;}
    @Override public String toString(){return name;}
}
