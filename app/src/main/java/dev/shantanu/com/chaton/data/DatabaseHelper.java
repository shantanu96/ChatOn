package dev.shantanu.com.chaton.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.Message;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.ui.adapters.ContactListAdapter;
import dev.shantanu.com.chaton.uitls.Util;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";

    private FirebaseFirestore db;

    private Context mContext;

    public DatabaseHelper(Context context) {
        mContext = context;
        db = FirebaseFirestore.getInstance();
    }

    //Only for testing purpose
    public DatabaseHelper(FirebaseFirestore firestore) {
        db = firestore;
    }

    public List<User> getAllUsers(final List<User> userList, final ContactListAdapter contactListAdapter) {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String loggedInUser = Util.getUserInfoFromSession(mContext).getId();
                        userList.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId().trim().equals(loggedInUser.trim()))
                                    continue;
                                User user = new User();
                                user.setId(document.getId());
                                user.setUserName((String) document.get("userName"));
                                user.setEmailId((String) document.get("emailId"));
                                userList.add(user);
                            }
                            contactListAdapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }

                });
        return userList;
    }


    public Task<Void> addMessage(Message message) {
        return db.collection("messages").document()
                .set(message);
    }

    public Task<Void> addLastMessageToConversation(Message message, String conversationId) {
        return db.collection("conversations").document(conversationId)
                .update("lstMsg", message);

    }

    public Task<Void> addUser(User user) {
        String userId = db.collection("users").document().getId();
        user.setId(userId);
        return db.collection("users").document(userId).set(user);
    }

    //Every user has unique email id
    public Task<QuerySnapshot> checkIfUserExists(String emailId) {
        return db.collection("users").whereEqualTo("emailId", emailId).get();
    }

    public Task<DocumentSnapshot> getUser(final String id) {
        final User user = new User();
        return db.collection("users").document(id)
                .get();
    }

    public Task<QuerySnapshot> getUserByEmailId(String emailId) {
        return db.collection("users").whereEqualTo("emailId", emailId).get();
    }

    public Task<QuerySnapshot> getAllMessages(String conversationId) {
        return db.collection("messages").orderBy("createdAt", Query.Direction.ASCENDING)
                .whereEqualTo("conversationId", conversationId)
                .get();
    }

    //Conversation methods
    public void addConversation(Conversation conversation) {
        final String conversationId = db.collection("conversations").document().getId();
        conversation.setId(conversationId);
        db.collection("conversations").document(conversationId)
                .set(conversation);
    }

    public Task<QuerySnapshot> checkConversationExists(final Conversation conversation, final String userId) {
        return db.collection("conversations")
                .get();
    }

    public Task<QuerySnapshot> getAllConversations() {
        return db.collection("conversations")
                .get();
    }

    //Profile Methods
    public Task<Void> updateUserName(String userId, String newUserName) {
        return db.collection("users").document(userId)
                .update("userName", newUserName);
    }

    public Task<Void> updateAvatar(String userId, String avatar) {
        return db.collection("users").document(userId)
                .update("avatar", avatar);
    }
}
