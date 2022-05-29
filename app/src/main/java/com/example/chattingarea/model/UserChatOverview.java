package com.example.chattingarea.model;

import android.graphics.Bitmap;

public class UserChatOverview {
    private String id;
    private String name;
    private String message;
    private String timestamp;
    private String urlAva;

    public UserChatOverview() {
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrlAva() {
        return urlAva;
    }

    public void setUrlAva(String urlAva) {
        this.urlAva = urlAva;
    }

    public UserChatOverview(String id, String name, String message, String timestamp, String urlAva) {
        this.id = id;
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
        this.urlAva = urlAva;
    }
}
