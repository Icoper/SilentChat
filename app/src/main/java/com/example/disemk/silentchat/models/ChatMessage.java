package com.example.disemk.silentchat.models;

/**
 * Created by disemk on 11.01.17.
 */

public class ChatMessage {
    private String text;
    private String name;
    private String photoUrl;
    private String room;
    private String uid;
    private String msgPhotoUrl;

    public ChatMessage() {
    }

    public ChatMessage(String text, String name, String photoUrl, String room, String uid) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.room = room;
        this.uid = uid;

    }

    public ChatMessage(String text, String name, String photoUrl, String room, String uid, String msgPhotoUrl) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.room = room;
        this.uid = uid;
        this.msgPhotoUrl = msgPhotoUrl;
    }

    public String getUid() {
        return uid;
    }

    public String getMsgPhotoUrl() {
        return msgPhotoUrl;
    }

    public void setMsgPhotoUrl(String msgPhotoUrl) {
        this.msgPhotoUrl = msgPhotoUrl;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
