package com.example.disemk.silentchat.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by disemk on 15.01.17.
 */

public class ChatRoom {
    String roomName;
    String roomKey;
    String creatorId;
    ArrayList<String> usersList;
    String usersCount;

    public ChatRoom() {
    }

    public ChatRoom(String roomName, String roomKey,
                    String creatorId, ArrayList<String> usersList, String usersCount) {
        this.roomName = roomName;
        this.usersCount = usersCount;
        this.roomKey = roomKey;
        this.creatorId = creatorId;
        this.usersList = usersList;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(String usersCount) {
        this.usersCount = usersCount;
    }

    public ArrayList<String> getUsersList() {
        return usersList;
    }

    public void setUsersList(ArrayList<String> usersList) {
        this.usersList = usersList;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getRoomKey() {
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
