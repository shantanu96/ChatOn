package dev.shantanu.com.chaton.ui;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;

import co.intentservice.chatui.ChatView;
import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.User;

public class ConversationActivity extends AppCompatActivity {

    private ChatView chatView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        chatView = findViewById(R.id.chat_view);
        databaseHelper = new DatabaseHelper(getApplicationContext());

        User user = (User) getIntent().getBundleExtra("bundle").getSerializable("user");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(user.getFirstName() + " " + user.getLastName());

        String userId = getApplicationContext().getSharedPreferences("MyPref", 0).getString("userId", "");

        HashMap<String, String> particpants = new HashMap();
        particpants.put(userId, "Shantanu Bhosale");
        particpants.put(user.getId(), user.getFirstName() + " " + user.getLastName());

        Conversation conversation = new Conversation();
        conversation.setParticipants(particpants);
        conversation.setCreatedTime(new Timestamp(new Date()));
        databaseHelper.checkConversationExists(conversation, userId);//checks if conversation exits otherwise adds the conversation
    }
}
