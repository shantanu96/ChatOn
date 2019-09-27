package dev.shantanu.com.chaton.data.entities;

import com.google.firebase.firestore.Exclude;
import com.stfalcon.chatkit.commons.models.IUser;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable, IUser {
    private String id;
    private String userName;
    private String emailId;
    private List<String> conversationIds;
    private String password;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    @Override
    public String getName() {
        return getUserName();
    }

    @Override
    public String getAvatar() {
        return null;
    }


    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public List<String> getConversationIds() {
        return conversationIds;
    }

    public void setConversationIds(List<String> conversationIds) {
        this.conversationIds = conversationIds;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
