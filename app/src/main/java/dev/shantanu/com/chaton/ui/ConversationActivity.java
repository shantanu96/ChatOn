package dev.shantanu.com.chaton.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
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

        otherUser = (User) getIntent().getBundleExtra("bundle").getSerializable("otherUser");
        currentUser = Util.getUserInfoFromSession(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(otherUser.getUserName());

        messageList = findViewById(R.id.messagesList);
        messageInput = findViewById(R.id.msgInput);
        adapter = new MessagesListAdapter<>(Util.getUserInfoFromSession(getApplicationContext()).getId(), null);
        messageList.setAdapter(adapter);


        List<User> userList = new ArrayList<>();
        userList.add(currentUser);
        userList.add(otherUser);

        conversation = new Conversation();
        conversation.setDialogName(otherUser.getName());
        conversation.setUsers(userList);
        conversation.setCreatedAt(new Date());


        //checks if conversation exits otherwise add the conversation
        databaseHelper.checkConversationExists(conversation, Util.getUserInfoFromSession(getApplicationContext()).getId())
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean conversationExists = false;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Conversation conve = document.toObject(Conversation.class);
                            if (conversation.getUsers().get(0).getId().equals(conve.getUsers().get(0).getId())
                                    && conversation.getUsers().get(1).getId().equals(conve.getUsers().get(1).getId())
                            ) {
                                conversation.setId(conve.getId());
                                conversationExists = true;
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
    }

    @Override
    public boolean onSubmit(final CharSequence input) {

        final Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setAuthor(currentUser.getId());
        message.setText(input.toString());
        message.setCreatedAt(new Date());
        message.setUser(currentUser);
        adapter.addToStart(message, false);
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
                        Log.d(TAG, "onComplete: Message added successfully");
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
                        adapter.addToEnd(messageList, true);
                    }
                });
    }
}
