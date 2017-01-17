package com.example.huanghaojian.linequery.model;

/**
 * Created by huanghaojian on 16/12/19.
 */

public class Address {

    public double latitude;

    public double longitude;


    public Address() {
    }

    public Address(double latitude, double longtitude) {
        this.latitude = latitude;
        this.longitude = longtitude;
    }
    public void setLatitude(double latitude){
        this.latitude=latitude;
    }
    public void setLongitude(double longitude){
        this.longitude=longitude;
    }
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
}

