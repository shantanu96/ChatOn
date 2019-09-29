package dev.shantanu.com.chaton;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;


import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.Conversation;
import dev.shantanu.com.chaton.data.entities.Message;
import dev.shantanu.com.chaton.data.entities.User;

@RunWith(MockitoJUnitRunner.class)
public class ConversationDatabaseTest {
    Conversation conversation = new Conversation();
    @Mock
    Context appContext;
    @Mock FirebaseFirestore firebaseFirestore;

    @Test
    public void addDataToConversation() {
        Message message = new Message();
        message.setText("hello");
        message.setAuthor("Shantanu");
        message.setCreatedAt(new Date());

        List<User> list = new ArrayList<>();
        User user = new User();
        user.setUserName("hatim");
        user.setEmailId("hatim@gmail.com");
        User user1 = new User();
        user.setUserName("shantanu");
        user.setEmailId("shantanu@gmail.com");
        list.add(user);

        conversation.setDialogName("Test");
        conversation.setDialogPhoto(null);
        conversation.setLastMessage(message);
        conversation.setUsers(list);

        DatabaseHelper databaseHelper = new DatabaseHelper(FirebaseFirestore.getInstance());
        databaseHelper.addConversation(conversation);
    }
}
