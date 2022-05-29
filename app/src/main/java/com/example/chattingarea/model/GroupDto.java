package com.example.chattingarea.model;

public class GroupDto {
    private String gId;
    private String gName;

    public String getgId() {
        return gId;
    }

    public void setgId(String gId) {
        this.gId = gId;
    }

    public String getgName() {
        return gName;
    }

    public void setgName(String gName) {
        this.gName = gName;
    }

    public GroupDto() {
    }

    public GroupDto(String gId, String gName) {
        this.gId = gId;
        this.gName = gName;
    }
}
