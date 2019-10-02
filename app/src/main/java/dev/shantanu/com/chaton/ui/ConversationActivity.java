package dev.shantanu.com.chaton.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.Message;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.uitls.Util;

public class ConversationActivity extends AppCompatActivity implements MessageInput.InputListener {

    private final String TAG = getClass().getSimpleName();

    private DatabaseHelper databaseHelper;

    private MessagesListAdapter<Message> adapter;
    private MessagesList messageList;
    private MessageInput messageInput;
    private Gson gson;
    private User currentUser, otherUser;
    private Conversation conversation;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        databaseHelper = new DatabaseHelper(getApplicationContext());
        db = FirebaseFirestore.getInstance();
        gson = new Gson();

        otherUser = (User) getIntent().getBundleExtra("bundle").getSerializable("otherUser");
        currentUser = Util.getUserInfoFromSession(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(otherUser.getUserName());

        messageList = findViewById(R.id.messagesList);
        messageInput = findViewById(R.id.msgInput);
        adapter = new MessagesListAdapter<>(Util.getUserInfoFromSession(getApplicationContext()).getId(), null);
        messageList.setAdapter(adapter);


        List<String> userIdList = new ArrayList<>();
        userIdList.add(currentUser.getId());
        userIdList.add(otherUser.getId());

        conversation = new Conversation();
        conversation.setDialogName(otherUser.getName());
        conversation.setParticipantsId(userIdList);
        conversation.setCreatedAt(new Date());


        //checks if conversation exits otherwise add the conversation
        databaseHelper.checkConversationExists(conversation, Util.getUserInfoFromSession(getApplicationContext()).getId())
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean conversationExists = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Conversation conve = document.toObject(Conversation.class);
                            Collections.sort(conversation.getParticipantsId());
                            Collections.sort(conve.getParticipantsId());

                            if (conversation.getParticipantsId().equals(conve.getParticipantsId())) {
                                conversationExists = true;
                                conversation.setId(conve.getId());
                                conversation.setLastMessageTime(new Date());
                                break;
                            }
                        }

                        //add conversation if does not exist
                        if (!conversationExists) {
                            databaseHelper.addConversation(conversation);
                        } else {
                            loadAllMessages();
                        }
                    }
                });

        messageInput.setInputListener(this);
        addNewMessages();
    }

    @Override
    public boolean onSubmit(final CharSequence input) {

        final Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setAuthor(currentUser.getId());
        message.setText(input.toString());
        message.setCreatedAt(new Date());
        message.setUser(currentUser);
        adapter.addToStart(message, true);
        databaseHelper.addMessage(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
//                        databaseHelper.addLastMessageToConversation(message, conversation.getId())
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        Log.d(TAG, "onComplete: Message added and conversation last message added successfully");
//                                    }
//                                });
                        db.collection("conversations").document(conversation.getId())
                                .update("lastMessageTime", new Date());
                        Log.d(TAG, "onComplete: Message added successfully");
                    }
                });


        return true;
    }

    public void loadAllMessages() {
        databaseHelper.getAllMessages(conversation.getId())
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final List<Message> messageList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            final Message message = new Message();
                            message.setId((String) document.getId());
                            message.setAuthor((String) document.get("author"));
                            message.setConversationId((String) document.get("conversationId"));
                            message.setCreatedAt((Date) document.get("createdAt"));
                            message.setText((String) document.get("text"));
                            if (message.getAuthor().equals(currentUser.getId())) {
                                message.setUser(currentUser);
                            } else {
                                message.setUser(otherUser);
                            }

                            messageList.add(message);
                        }
                        adapter.addToEnd(messageList, true);
                    }
                });
    }

    public void addNewMessages() {
        FirebaseFirestore.getInstance().collection("messages").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        if (dc.getDocument().get("conversationId").equals(conversation.getId()) && dc.getDocument().get("author").equals(otherUser.getId())) {
                            Message message = new Message();
                            message.setId((String) dc.getDocument().getId());
                            message.setAuthor((String) dc.getDocument().get("author"));
                            message.setConversationId((String) dc.getDocument().get("conversationId"));
                            message.setCreatedAt((Date) dc.getDocument().get("createdAt"));
                            message.setText((String) dc.getDocument().get("text"));
                            message.setUser(otherUser);
                            adapter.addToStart(message, true);
                        }
                    }
//                for (DocumentSnapshot document:querySnapshot){
//
//                    if (document.get("conversationId").equals(conversation.getId()) && document.get("author").equals(otherUser.getId())) {
//                        Message message = new Message();
//                        message.setId((String) document.getId());
//                        message.setAuthor((String) document.get("author"));
//                        message.setConversationId((String) document.get("conversationId"));
//                        message.setCreatedAt((Date) document.get("createdAt"));
//                        message.setText((String) document.get("text"));
//                        message.setUser(otherUser);
//                        adapter.addToStart(message, true);
//
//                    }
//                }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
