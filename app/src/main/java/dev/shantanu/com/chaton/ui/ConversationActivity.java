package dev.shantanu.com.chaton.ui;

import android.os.Bundle;

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

import java.util.Date;
import java.util.HashMap;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.Message;
import dev.shantanu.com.chaton.data.entities.User;

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
        currentUser = (User) gson.fromJson(
                getSharedPreferences("MyPref", 0).getString("User", "")
                , User.class);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(otherUser.getFirstName() + " " + otherUser.getLastName());

        final String userId = getApplicationContext().getSharedPreferences("MyPref", 0).getString("userId", "");

        messageList = findViewById(R.id.messagesList);
        messageInput = findViewById(R.id.msgInput);
        adapter = new MessagesListAdapter<>(userId, null);
        messageList.setAdapter(adapter);

        HashMap<String, String> particpants = new HashMap();
        particpants.put(userId, "Shantanu Bhosale");
        particpants.put(otherUser.getId(), otherUser.getFirstName() + " " + otherUser.getLastName());

        conversation = new Conversation();
        conversation.setParticipants(particpants);
        conversation.setCreatedTime(new Timestamp(new Date()));
        databaseHelper.checkConversationExists(conversation, userId);//checks if conversation exits otherwise add the conversation

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
}
