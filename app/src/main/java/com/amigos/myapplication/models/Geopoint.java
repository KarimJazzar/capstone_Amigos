package com.amigos.myapplication.models;

public class Geopoint {
    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setCoordinates(double lat, double lng) {
        this.longitude = lng;
    }
}
