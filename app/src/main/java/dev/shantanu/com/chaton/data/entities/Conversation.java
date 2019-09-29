package dev.shantanu.com.chaton.data.entities;

import com.google.firebase.Timestamp;
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
    private List<User> users;
    private Message lstMsg;
    private IMessage lastMessage;
    private int unreadCount;
    private Date createdAt;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Exclude
    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Exclude
    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Exclude
    @Override
    public void setLastMessage(IMessage message) {
        this.lastMessage = message;
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

    @Exclude
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

    public Message getLstMsg() {
        return lstMsg;
    }

    public void setLstMsg(Message lstMsg) {
        this.lstMsg = lstMsg;
        setLastMessage(lstMsg);
    }
}
