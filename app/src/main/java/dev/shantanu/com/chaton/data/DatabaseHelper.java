package dev.shantanu.com.chaton.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;

import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.Message;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.ui.adapters.ChatListAdapter;
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
                                user.setFirstName((String) document.get("firstName"));
                                user.setLastName((String) document.get("lastName"));
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

    public List<Conversation> getAllConversations(final List<Conversation> conversationList, final ChatListAdapter conversationListAdapter) {
        db.collection("conversations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    String loggedInUser = Util.getUserInfoFromSession(mContext).getId();

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        conversationList.clear();
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap<String, String> participants = (HashMap<String, String>) document.get("participants");

                                if (participants != null && participants.containsKey(loggedInUser)) {
                                    Conversation c = new Conversation();
                                    c.setId((String) document.getId());
                                    c.setParticipants(participants);
                                    conversationList.add(c);
                                }
                            }
                            conversationListAdapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }

                    }
                });
        return conversationList;
    }

    public void addMessage(Message message) {
        db.collection("messages").document()
                .set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext, "Message sent", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void addConversation(Conversation conversation, final String userId) {
        final String conversationId = db.collection("conversations").document().getId();
        db.collection("conversations").document(conversationId)
                .set(conversation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db.collection("users").document(userId)
                                .update("conversationIds", FieldValue.arrayUnion(conversationId));
                        Toast.makeText(mContext, "Convo added", Toast.LENGTH_LONG).show();
                    }
                });


    }

    public Task<Void> addUser(User user) {
        String userId = db.collection("users").document().getId();
        user.setId(userId);
        return db.collection("users").document().set(user);
    }


    public void checkConversationExists(final Conversation conversation, final String userId) {
        db.collection("conversations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean conversationExists = false;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap<String, String> participants = (HashMap<String, String>) document.get("participants");
                                if (participants != null && participants.equals(conversation.getParticipants())) {
                                    conversationExists = true;
                                    break;
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }

                        //add conversation if does not exist
                        if (!conversationExists) {
                            addConversation(conversation, userId);
                        }
                    }
                });
    }

    //Every user has unique email id
    public Task<QuerySnapshot> checkIfUserExists(String emailId) {
        return db.collection("users").whereEqualTo("emailId", emailId).get();
    }

    public Task<QuerySnapshot> getConversationId(Conversation conversation) {
        return db.collection("conversations")
                .whereEqualTo("participants", conversation.getParticipants()).get();
    }

    public Task<DocumentSnapshot> getUser(final String id) {
        final User user = new User();
        return db.collection("users").document(id)
                .get();
    }

    public Task<QuerySnapshot> getUserByEmailId(String emailId) {
        return db.collection("users").whereEqualTo("emailId", emailId).get();
    }
}
