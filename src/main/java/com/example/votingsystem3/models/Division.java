package com.example.votingsystem3.models;
public class Division {
    private int id; private String name;
    public Division(int id, String name) { this.id=id; this.name=name; }
    public int getId(){return id;} public String getName(){return name;}
    @Override public String toString(){return name;}
}
