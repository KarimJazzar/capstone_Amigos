package com.amigos.myapplication.models;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Trip implements Serializable {
    private String from;
    private Geopoint fromPoints;
    private String to;
    private Geopoint toPoints;
    private Integer seats;
    private Date date;
    private String time;
    private Double price;
    private List<String> conditions;
    private User driver;
    private List<User> passengers;
    private List<String> users;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Geopoint getFromPoints() {
        return fromPoints;
    }

    public void setFromPoints(Geopoint fromPoints) {
        this.fromPoints = fromPoints;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Geopoint getToPoints() {
        return toPoints;
    }

    public void setToPoints(Geopoint toPoints) {
        this.toPoints = toPoints;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public User getDriver() {
        return driver;
    }

    public void setDriver(User driver) {
        this.driver = driver;
    }

    public List<User> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<User> passengers) {
        this.passengers = passengers;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}
