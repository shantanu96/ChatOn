package dev.shantanu.com.chaton.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.uitls.Util;

public class MainActivity extends AppCompatActivity implements DialogsListAdapter.OnDialogClickListener {

    private final String TAG = getClass().getSimpleName();


    private DatabaseHelper databaseHelper;
    private FloatingActionButton floatingActionButton;
    private DialogsList dialogsList;
    private DialogsListAdapter dialogsListAdapter;

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private View headerLayout;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        floatingActionButton = findViewById(R.id.floatingActionButton);
        dialogsList = findViewById(R.id.dialogsList);

        dialogsListAdapter = new DialogsListAdapter(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                if (url == null || url.isEmpty()) {
                    Glide.with(getApplicationContext())
                            .load(Util.DEFAULT_PROFILE_IMAGE_URL)
                            .into(imageView);
                } else {
                    Glide.with(getApplicationContext())
                            .load(url)
                            .into(imageView);
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

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // This will display an Up icon (<-), we will replace it with hamburger later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawerToggle = setupDrawerToggle();

        // Setup toggle to display hamburger icon with nice animation
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerToggle.syncState();

        mDrawer.addDrawerListener(drawerToggle);
        // Setup drawer view
        nvDrawer = findViewById(R.id.nvView);
        setupDrawerContent(nvDrawer);
        headerLayout = nvDrawer.inflateHeaderView(R.layout.nav_header);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
        setHeaderImageView();
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_logout:
                Util.clearPreferneces(getApplicationContext());
                Util.logout(getApplicationContext());
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadConversations() {
        dialogsListAdapter.clear();
        db.collection("conversations")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                final List<Conversation> conversationList = querySnapshot.toObjects(Conversation.class);
                final List<Conversation> currentUserConversationList = new ArrayList<>();
                for (Conversation c : conversationList) {
                    if (Util.getUserInfoFromSession(getApplicationContext()).getConversationIds().contains(c.getId())) {
                        final User currentUser = Util.getUserInfoFromSession(getApplicationContext());
                        User otherUser = (User) c.getUsers().stream().filter(u -> !((User) u).getId().equals(currentUser.getId())).collect(Collectors.toList()).get(0);

                        ArrayList<User> participantList = new ArrayList<>();
                        participantList.add(currentUser);
                        participantList.add(otherUser);

                        c.setUsers(participantList);
                        c.setDialogName(otherUser.getUserName());
                        c.setDialogPhoto(otherUser.getAvatar());
                        currentUserConversationList.add(c);
                    }
                }
                dialogsListAdapter.addItems(currentUserConversationList);
                dialogsListAdapter.notifyDataSetChanged();
            }
        });

//        db.collection("users").get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot snapshot) {
//                        final HashMap<String, User> userList = new HashMap<>();
//                        for (User u : snapshot.toObjects(User.class)) {
//                            userList.put(u.getId(), u);
//                        }
//                        db.collection("conversations")
//                                .whereArrayContains("participantsId", Util.getUserInfoFromSession(getApplicationContext()).getId())
//                                .get()
//                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onSuccess(QuerySnapshot snapshot) {
//                                        final List<Conversation> conversationList = snapshot.toObjects(Conversation.class);
//                                        for (Conversation c : conversationList) {
//                                            String currentUserId = Util.getUserInfoFromSession(getApplicationContext()).getId();
//                                            String otherUserId = c.getParticipantsId().get(0).equals(currentUserId) ?
//                                                    c.getParticipantsId().get(1) : c.getParticipantsId().get(0);
//
//                                            User currentUser = userList.get(currentUserId);
//                                            User otherUser = userList.get(otherUserId);
//
//                                            ArrayList<User> participantList = new ArrayList<>();
//                                            participantList.add(currentUser);
//                                            participantList.add(otherUser);
//
//                                            c.setUsers(participantList);
//                                            c.setDialogName(otherUser.getUserName());
//                                            c.setDialogPhoto(otherUser.getAvatar());
//                                        }
//                                        dialogsListAdapter.addItems(conversationList);
//                                        dialogsListAdapter.notifyDataSetChanged();
//                                    }
//                                });
//                    }
//                });
    }

    @Override
    public void onDialogClick(IDialog dialog) {
        Conversation conversation = (Conversation) dialog;
        String currentUserId = Util.getUserInfoFromSession(getApplicationContext()).getId();
        User user = conversation.getUsers().get(0).getId().equals(currentUserId) ?
                (User) conversation.getUsers().get(1) : (User) conversation.getUsers().get(0);
        databaseHelper.getUser(user.getId())
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        User otherUser = task.getResult().toObject(User.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("otherUser", (Serializable) otherUser);
                        Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
                        intent.putExtra("bundle", bundle);
                        startActivity(intent);
                    }
                });

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked

        switch (menuItem.getItemId()) {
            case R.id.nav_logout:
                Util.clearPreferneces(getApplicationContext());
                Util.logout(getApplicationContext());
                Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
                i2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i2);
                finish();
                break;
            case R.id.nav_profile:
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                break;
            default:

        }


    }

    public void setHeaderImageView() {
        CircleImageView ivHeaderPhoto = headerLayout.findViewById(R.id.nav_profile_img);
        if (Util.getUserInfoFromSession(getApplicationContext()).getAvatar() == null) {
            Glide.with(getApplicationContext())
                    .load(Util.DEFAULT_PROFILE_IMAGE_URL)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivHeaderPhoto);
        } else {
            Glide.with(getApplicationContext())
                    .load(Util.getUserInfoFromSession(getApplicationContext()).getAvatar())
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivHeaderPhoto);
        }

    }

}
