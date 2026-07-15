package com.example.votingsystem3.models;
public class Upazila {
    private int id; private String name; private int districtId;
    public Upazila(int id, String name, int districtId) {this.id=id; this.name=name; this.districtId=districtId;}
    public int getId(){return id;} public String getName(){return name;}
    @Override public String toString(){return name;}
}
