package com.example.chattingarea.model;

import android.content.Context;

import com.example.chattingarea.Constant;

import java.util.Date;

public class Contact {
    private String auth;
    private String destination;
    private String status;
    private long time;


    public Contact() {
    }

    public static Contact createRequest(String auth, String destination){
        return new Contact(auth, destination, Constant.StatusContacts.REQUEST,new Date().getTime());
    }

    public Contact(String auth, String destination, String status, long time) {
        this.auth = auth;
        this.destination = destination;
        this.status = status;
        this.time = time;
    }

    public static boolean isMyContact(String myUId, Contact contact) {
        if(contact == null) return false;
        if(contact.getAuth().equals(myUId))
            return true;
        return contact.getDestination().equals(myUId);
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
