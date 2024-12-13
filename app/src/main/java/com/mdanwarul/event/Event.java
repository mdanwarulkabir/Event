package com.mdanwarul.event;

import java.io.Serializable;

public class Event implements Serializable {
    private String id;
    private String name;
    private String date;
    private String description;
    private String userId;
    private String userIdDate; // New field for unique user-date validation

    // Constructor with all fields
    public Event(String id, String name, String date, String description, String userId) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.description = description;
        this.userId = userId;
        this.userIdDate = userId + "_" + date; // Combine userId and date for unique identification
    }

    // Default constructor for Firebase
    public Event() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        this.userIdDate = this.userId + "_" + date; // Update userIdDate when date is set
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        this.userIdDate = userId + "_" + this.date; // Update userIdDate when userId is set
    }

    public String getUserIdDate() {
        return userIdDate;
    }

    public void setUserIdDate(String userIdDate) {
        this.userIdDate = userIdDate;
    }
}
