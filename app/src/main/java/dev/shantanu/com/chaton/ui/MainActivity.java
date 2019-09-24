package dev.shantanu.com.chaton.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.ui.adapters.ChatListAdapter;

public class MainActivity extends AppCompatActivity implements ChatListAdapter.ChatListItemListener {

    private final String TAG = getClass().getSimpleName();

    private RecyclerView rvChatList;
    private DatabaseHelper databaseHelper;
    private List<Conversation> conversationList;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floatingActionButton = findViewById(R.id.floatingActionButton);

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPref", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userId", "oLoTWwZoxtPUXGhlO95z");
        editor.commit();

        databaseHelper = new DatabaseHelper(this);

        rvChatList = findViewById(R.id.rv_chat_list);

        conversationList = new ArrayList<>();
        //send logged in userid to find other participant id in hashmap(improve the logic)
        ChatListAdapter chatListAdapter = new ChatListAdapter(conversationList, this,
                getApplicationContext().getSharedPreferences("MyPref", 0).getString("userId", ""));
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
    public void onClick(int pos, String recieverUserId) {
        GetUserTask gt = new GetUserTask();
        gt.execute(recieverUserId);
    }

    private class GetUserTask extends AsyncTask<String, Integer, User> {

        @Override
        protected User doInBackground(String... ids) {
            User user = databaseHelper.getUser(ids[0]);
            Log.d(TAG, "doInBackground: ");
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("user", (Serializable) user);
            Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
            intent.putExtra("bundle", bundle);
            startActivity(intent);
        }
    }
}
