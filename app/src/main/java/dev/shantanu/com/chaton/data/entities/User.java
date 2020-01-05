package dev.shantanu.com.chaton.data.entities;

import com.google.firebase.firestore.Exclude;
import com.stfalcon.chatkit.commons.models.IUser;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable, IUser {
    private String id;
    private String userName;
    private String emailId;
    private String password;
    private String avatar;
    private List<String> conversationIds;

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
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
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

    public List<String> getConversationIds() {
        return conversationIds;
    }

    public void setConversationIds(List<String> conversationIds) {
        this.conversationIds = conversationIds;
    }
}
