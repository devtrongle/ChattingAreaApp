package com.example.chattingarea;

public final class Constant {
    public static final String USER_REF = "user";
    public static final String CONTACTS_REF = "contacts";
    public static final String ROOM_REF = "room";
    public static final String GROUP_REF = "group";
    public static final String GROUP_Chat_REF = "groupChat";

    public static final String TEXT = "text";
    public static final String IMAGE = "image";
    public static final String VIDEO = "video";

    public static final class StatusContacts {
        public static final String FRIEND = "friend";
        public static final String DENY = "deny";
        public static final String REQUEST = "request";
    }


    public enum StatusRequest{
        SUCCESS,
        NO_DATA,
        FAIL,
        EXIST,
    }
}
