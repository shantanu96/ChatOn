package dev.shantanu.com.chaton.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.User;

public class RegisterActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private EditText etUserName, etEmail, etPass;
    private Button btnRegister;
    private TextView loginLink;

    private FirebaseAuth mAuth;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseHelper = new DatabaseHelper(getApplicationContext());

        etUserName = findViewById(R.id.et_register_user_name);
        etEmail = findViewById(R.id.et_register_email);
        etPass = findViewById(R.id.et_register_pass);
        loginLink = findViewById(R.id.link_login);
        btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerWithEmailAndPassword(etEmail.getText().toString(), etPass.getText().toString());
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    public void registerWithEmailAndPassword(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) {
                                //add user to database
                                databaseHelper.checkIfUserExists(etEmail.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.getResult().size() == 0) {
                                                    User user = new User();
                                                    user.setUserName(etUserName.getText().toString());
                                                    user.setEmailId(etEmail.getText().toString());
                                                    user.setPassword(etPass.getText().toString());
                                                    databaseHelper.addUser(user)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    startActivity(intent);
                                                                }
                                                            });
                                                } else {
                                                    Snackbar.make(findViewById(R.id.activity_register),
                                                            "User with given email already registered", Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
