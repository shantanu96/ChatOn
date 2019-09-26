package dev.shantanu.com.chaton.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.ui.adapters.ContactListAdapter;

public class ContactListActivity extends AppCompatActivity implements ContactListAdapter.ContactListItemListener {

    private RecyclerView rvContactList;
    private DatabaseHelper databaseHelper;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        getSupportActionBar().setTitle("Contacts");

        rvContactList = findViewById(R.id.rv_contact_list);

        databaseHelper = new DatabaseHelper(this);

        userList = new ArrayList<>();
        ContactListAdapter contactListAdapter = new ContactListAdapter(userList, this);
        databaseHelper.getAllUsers(userList, contactListAdapter);

        rvContactList.setLayoutManager(new LinearLayoutManager(this));
        rvContactList.setAdapter(contactListAdapter);
    }

    @Override
    public void onClick(int pos) {
        User user = userList.get(pos);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", (Serializable) user);
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }
}
