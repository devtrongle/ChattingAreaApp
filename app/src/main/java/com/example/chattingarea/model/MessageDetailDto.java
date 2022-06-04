package com.example.chattingarea.model;

import com.example.chattingarea.Constant;

import java.util.Date;

public class MessageDetailDto {

    private String messageId;
    private String content;
    private Date timestamp;
    private boolean isStringType;
    private String type;

    private String uId;
    private String uName;
    private String uAva;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isStringType() {
        return isStringType;
    }

    public String getType() {
        return type == null ? Constant.TEXT : type.equals("") ?  Constant.TEXT : type;

    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStringType(boolean stringType) {
        isStringType = stringType;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getuAva() {
        return uAva;
    }

    public void setuAva(String uAva) {
        this.uAva = uAva;
    }

    public MessageDetailDto() {
    }

    public MessageDetailDto(String messageId, String content, Date timestamp, boolean isStringType, String uId, String uName, String uAva, String type) {
        this.messageId = messageId;
        this.content = content;
        this.timestamp = timestamp;
        this.isStringType = isStringType;
        this.uId = uId;
        this.uName = uName;
        this.uAva = uAva;
        this.type = type;
    }
}
