package dev.shantanu.com.chaton.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.ui.adapters.ChatListAdapter;
import dev.shantanu.com.chaton.uitls.Util;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();


    private DatabaseHelper databaseHelper;
    private FloatingActionButton floatingActionButton;
    private DialogsList dialogsList;
    private DialogsListAdapter dialogsListAdapter;

    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Chats");

        floatingActionButton = findViewById(R.id.floatingActionButton);
        dialogsList = findViewById(R.id.dialogsList);

        dialogsListAdapter = new DialogsListAdapter(R.layout.activity_main, new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {

            }
        });

        dialogsList.setAdapter(dialogsListAdapter);

        databaseHelper = new DatabaseHelper(this);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
                startActivity(intent);
            }
        });

        loadConversations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
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
            Util.logout(getApplicationContext());
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadConversations() {
        databaseHelper.getAllConversations()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<Conversation> conversationList = new ArrayList<>();
                        String currentUserId = Util.getUserInfoFromSession(getApplicationContext()).getId();

                        for (QueryDocumentSnapshot document : task.getResult()) {

                            if (task.getResult().size() != 0) {
                                Conversation conversation = document.toObject(Conversation.class);

                                User otherUser = conversation.getUsers().get(0).getId().equals(currentUserId) ?
                                        (User) conversation.getUsers().get(1) : (User) conversation.getUsers().get(0);

                                if (conversation.getUsers().get(0).getId().equals(currentUserId) ||
                                        conversation.getUsers().get(1).getId().equals(currentUserId)) {
                                    conversation.setDialogName(otherUser.getUserName());
                                    conversationList.add(conversation);
                                }
                            }

                            dialogsListAdapter.addItems(conversationList);
                            dialogsListAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
