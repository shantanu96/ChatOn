package dev.shantanu.com.chaton.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.ui.adapters.ChatListAdapter;
import dev.shantanu.com.chaton.uitls.Util;

public class MainActivity extends AppCompatActivity implements ChatListAdapter.ChatListItemListener {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView rvChatList;
    private ChatListAdapter chatListAdapter;
    private DatabaseHelper databaseHelper;
    private List<Conversation> conversationList;
    private FloatingActionButton floatingActionButton;

    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Chats");

        floatingActionButton = findViewById(R.id.floatingActionButton);

        databaseHelper = new DatabaseHelper(this);

        rvChatList = findViewById(R.id.rv_chat_list);

        conversationList = new ArrayList<>();
        //send logged in userid to find other participant id in hashmap(improve the logic)
        chatListAdapter = new ChatListAdapter(conversationList, this,
                Util.getUserInfoFromSession(getApplicationContext()).getId());
        databaseHelper.getAllConversations(conversationList, chatListAdapter);

        rvChatList.setLayoutManager(new LinearLayoutManager(this));
        rvChatList.setAdapter(chatListAdapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseHelper.getAllConversations(conversationList, chatListAdapter);
    }

    @Override
    public void onClick(int pos, final String receiverUserId) {
        Task<DocumentSnapshot> userDocument = databaseHelper.getUser(receiverUserId);
        userDocument.addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    User user = new User();
                    user.setId(receiverUserId);
                    user.setEmailId((String) document.get("emailId"));
                    user.setFirstName((String) document.get("firstName"));
                    user.setLastName((String) document.get("lastName"));

                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", (Serializable) user);
                    Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btn_logout) {
            Util.clearPreferneces(getApplicationContext());
            Util.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
