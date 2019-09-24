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
                        String loggedInUser = mContext.getSharedPreferences("MyPref", 0).getString("userId", "");

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
                    String loggedInUser = mContext.getSharedPreferences("MyPref", 0).getString("userId", "");

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap<String, String> participants = (HashMap<String, String>) document.get("participants");

                                if (participants.containsKey(loggedInUser)) {
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
                                if (participants.equals(conversation.getParticipants())) {
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

    public User getUser(final String id) {
        final User user = new User();
        db.collection("users").document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            user.setId(id);
                            user.setEmailId((String) document.get("emailId"));
                            user.setFirstName((String) document.get("firstName"));
                            user.setLastName((String) document.get("lastName"));
//                            user.setCoverssataionIds((List<String>) document.get("conversationIds"));
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

        return user;
    }
}
