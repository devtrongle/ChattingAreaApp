package com.example.chattingarea.model;

import android.content.Context;

import com.example.chattingarea.Constant;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Contact implements Serializable {

    private String id;
    private String auth;
    private String destination;
    private String status;
    private long time;


    public Contact() {
    }

    public static Contact createRequest(String id,String auth, String destination){
        return new Contact(id, auth, destination, Constant.StatusContacts.REQUEST,new Date().getTime());
    }

    public Contact(String id, String auth, String destination, String status, long time) {
        this.id = id;
        this.auth = auth;
        this.destination = destination;
        this.status = status;
        this.time = time;
    }

    public static boolean isMyContact(String myUId, Contact contact, List<Contact> listContacts) {
        if(contact == null) return false;
        if(contact.getAuth().equals(myUId) || contact.getDestination().equals(myUId)){
            for(Contact c : listContacts){
                if( c.getAuth().equals(contact.getAuth()) &&
                        contact.getDestination().equals(c.getDestination())){
                    return false;
                }
            }
            return true;
        }
        return false;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
