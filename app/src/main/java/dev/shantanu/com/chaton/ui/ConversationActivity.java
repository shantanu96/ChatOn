package dev.shantanu.com.chaton.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        databaseHelper = new DatabaseHelper(getApplicationContext());
        gson = new Gson();

        otherUser = (User) getIntent().getBundleExtra("bundle").getSerializable("user");
        currentUser = Util.getUserInfoFromSession(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(otherUser.getFirstName() + " " + otherUser.getLastName());

        messageList = findViewById(R.id.messagesList);
        messageInput = findViewById(R.id.msgInput);
        adapter = new MessagesListAdapter<>(Util.getUserInfoFromSession(getApplicationContext()).getId(), null);
        messageList.setAdapter(adapter);

        HashMap<String, String> particpants = new HashMap();
        particpants.put(currentUser.getId(), currentUser.getFirstName() + " " + currentUser.getLastName());
        particpants.put(otherUser.getId(), otherUser.getFirstName() + " " + otherUser.getLastName());

        conversation = new Conversation();
        conversation.setParticipants(particpants);
        conversation.setCreatedTime(new Timestamp(new Date()));

        //checks if conversation exits otherwise add the conversation
        databaseHelper.checkConversationExists(conversation, Util.getUserInfoFromSession(getApplicationContext()).getId())
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean conversationExists = false;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                HashMap<String, String> participants = (HashMap<String, String>) document.get("participants");
                                if (participants != null && participants.equals(conversation.getParticipants())) {
                                    conversationExists = true;
                                    conversation.setId((String) document.getId());
                                    break;
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }

                        //add conversation if does not exist
                        if (!conversationExists) {
                            databaseHelper.addConversation(conversation, currentUser.getId());
                        } else {
                            loadAllMessages();
                        }
                    }
                });

        messageInput.setInputListener(this);
    }

    @Override
    public boolean onSubmit(final CharSequence input) {

        databaseHelper.getConversationId(conversation)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        String conversationId = "";
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            conversationId = document.getId();
                        }
                        Message message = new Message();
                        message.setConversationId(conversationId);
                        message.setAuthor(currentUser.getId());
                        message.setText(input.toString());
                        message.setCreatedAt(new Date());
                        message.setUser(currentUser);
                        adapter.addToStart(message, false);
                        databaseHelper.addMessage(message);
                    }
                });
        return true;
    }

    public void loadAllMessages() {
        databaseHelper.getMessagesByConversationId(conversation.getId())
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
                        adapter.addToEnd(messageList, false);
                    }
                });
    }
}
