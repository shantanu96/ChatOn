package dev.shantanu.com.chaton.data.entities;

import com.google.firebase.firestore.Exclude;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;
import java.util.List;

public class Conversation implements IDialog {
    private String id;
    private String dialogPhoto;
    private String dialogName;
    @Exclude
    private List<User> users;
    @Exclude
    private Message lastMessage;
    private Date lastMessageTime;
    private List<String> participantsId;
    private int unreadCount;
    private Date createdAt;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    public void setDialogPhoto(String dialogPhoto) {
        this.dialogPhoto = dialogPhoto;
    }

    @Exclude
    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Exclude
    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
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

    @Exclude
    @Override
    public Message getLastMessage() {
        return lastMessage;
    }

    @Exclude
    @Override
    public void setLastMessage(IMessage message) {
        this.lastMessage = (Message) message;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public List<String> getParticipantsId() {
        return participantsId;
    }

    public void setParticipantsId(List<String> participantsId) {
        this.participantsId = participantsId;
    }
}
