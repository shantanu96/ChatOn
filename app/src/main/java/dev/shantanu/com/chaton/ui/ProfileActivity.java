package dev.shantanu.com.chaton.ui;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.DatabaseHelper;
import dev.shantanu.com.chaton.data.entities.User;
import dev.shantanu.com.chaton.uitls.StorageHelper;
import dev.shantanu.com.chaton.uitls.Util;

public class ProfileActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 123;
    private final String TAG = getClass().getSimpleName();

    private TextView userName;
    private CircleImageView profileImage;
    private ImageView editBtnImage;

    private DatabaseHelper databaseHelper;
    private StorageHelper storageHelper;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        isReadStoragePermissionGranted();

        databaseHelper = new DatabaseHelper(getApplicationContext());
        storageHelper = new StorageHelper();
        user = Util.getUserInfoFromSession(getApplicationContext());

        userName = findViewById(R.id.profile_user_name);
        profileImage = findViewById(R.id.profile_profile_img);
        editBtnImage = findViewById(R.id.profile_image_edit_btn);

        userName.setText(user.getUserName());
        loadImageInProfileImage();

        editBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUserNamePopUp();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = Util.getUserInfoFromSession(getApplicationContext());
    }

    public void loadImageInProfileImage() {
        String avatar = user.getAvatar();
        if (avatar == null || avatar.isEmpty()) {
            Glide.with(getApplicationContext())
                    .load(Util.DEFAULT_PROFILE_IMAGE_URL)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);
        } else {
            Glide.with(getApplicationContext())
                    .load(avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);
        }
    }

    public void editUserNamePopUp() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit");
        alert.setMessage("Change Username");

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                final String newUserName = input.getText().toString();
                databaseHelper.updateUserName(user.getId(), newUserName)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                user.setUserName(newUserName);
                                Util.saveUserInfoInSession(getApplicationContext(), user);
                                userName.setText(newUserName);
                            }
                        });
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String picturePath = cursor.getString(columnIndex);
            cursor.close();

            StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();

            // Add image to storage
            Uri file = Uri.fromFile(new File(picturePath));
            final StorageReference storageReference = mStorageReference.child(user.getId());
            storageReference.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    //Add image uri to firestore
                                    databaseHelper.updateAvatar(user.getId(), uri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    user.setAvatar(uri.toString());
                                                    Util.saveUserInfoInSession(getApplicationContext(), user);
                                                    loadImageInProfileImage();
                                                }
                                            });
                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG, "onFailure: " + exception.getLocalizedMessage());
                }
            });
            ;

        }
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted1");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }
}
