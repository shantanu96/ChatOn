package dev.shantanu.com.chaton.uitls;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class StorageHelper {
    private StorageReference mStorageReference;

    public StorageHelper() {
        mStorageReference = FirebaseStorage.getInstance().getReference();
    }

    public UploadTask uploadFile(String userId, String filePath) {
        Uri file = Uri.fromFile(new File(filePath));
        StorageReference riversRef = mStorageReference.child(userId);

        return riversRef.putFile(file);
    }
}
