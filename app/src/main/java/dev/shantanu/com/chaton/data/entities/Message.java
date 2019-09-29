package dev.shantanu.com.chaton.data.entities;


import com.google.firebase.firestore.Exclude;
import com.stfalcon.chatkit.commons.models.IMessage;

import java.io.Serializable;
import java.util.Date;

public class Message implements IMessage, Serializable {

    private String id;
    private String text;
    private String conversationId;
    String author;
    private Date createdAt;
    @Exclude
    private User user;

    public Message() {
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    @Exclude
    public User getUser() {
        return user;
    }

    @Exclude
    public void setUser(User user) {
        this.user = user;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
