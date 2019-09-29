package dev.shantanu.com.chaton.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.uitls.Util;

public class MainActivity extends AppCompatActivity implements DialogsListAdapter.OnDialogClickListener {

    private final String TAG = getClass().getSimpleName();
    private final String DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/chaton-bb63b.appspot.com/o/default_profile_img.png?alt=media&token=bdd3d4cd-5885-409f-9ccb-4642bcd5bb58";


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

        dialogsListAdapter = new DialogsListAdapter(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                if (url == null || url.isEmpty()) {
                    Picasso.get().load(DEFAULT_PROFILE_IMAGE_URL).into(imageView);
                } else {
                    Picasso.get().load(url).into(imageView);
                }

            }
        });
        dialogsListAdapter.setOnDialogClickListener(this);

        dialogsList.setAdapter(dialogsListAdapter);

        databaseHelper = new DatabaseHelper(this);


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
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadConversations() {
        dialogsListAdapter.clear();
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

    @Override
    public void onDialogClick(IDialog dialog) {
        Conversation conversation = (Conversation) dialog;
        String currentUserId = Util.getUserInfoFromSession(getApplicationContext()).getId();
        User otherUser = conversation.getUsers().get(0).getId().equals(currentUserId) ?
                (User) conversation.getUsers().get(1) : (User) conversation.getUsers().get(0);
        Bundle bundle = new Bundle();
        bundle.putSerializable("otherUser", (Serializable) otherUser);
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }
}
