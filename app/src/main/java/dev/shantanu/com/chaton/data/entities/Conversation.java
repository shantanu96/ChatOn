package dev.shantanu.com.chaton.data.entities;

import com.google.firebase.Timestamp;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;
import java.util.List;

public class Conversation implements IDialog {
    private String id;
    private String dialogPhoto;
    private String dialogName;
    private List<User> users;
    private Message lastMessage;
    private int unreadCount;
    private Date createdAt;

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getDialogPhoto() {
        return null;
    }

    @Override
    public String getDialogName() {
        return null;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        this.lastMessage =(Message)message;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = dialogPhoto;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
