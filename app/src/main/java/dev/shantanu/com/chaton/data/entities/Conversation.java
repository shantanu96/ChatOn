package dev.shantanu.com.chaton.data.entities;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.List;

public class Conversation {

    private String id;
    private HashMap<String,String> participants;
    private Timestamp createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getParticipants() {
        return participants;
    }

    public void setParticipants(HashMap<String, String> participants) {
        this.participants = participants;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
}
