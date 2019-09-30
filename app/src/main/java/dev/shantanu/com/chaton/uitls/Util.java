package dev.shantanu.com.chaton.uitls;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import dev.shantanu.com.chaton.R;
import dev.shantanu.com.chaton.data.entities.User;

public class Util {
    public static final String DEFAULT_PROFILE_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/chaton-bb63b.appspot.com/o/default_profile_img.png?alt=media&token=bdd3d4cd-5885-409f-9ccb-4642bcd5bb58";

    public static void saveUserInfoInSession(Context context, User user) {
        Gson gson = new Gson();
        String json = gson.toJson(user);
        SharedPreferences.Editor editor = getSharedPreferenceEditor(context);
        editor.putString("User", json);
        editor.commit();
    }

    public static User getUserInfoFromSession(Context context) {
        Gson gson = new Gson();
        return gson.fromJson(
                getPreferences(context).getString("User", ""), User.class);
    }

    public static SharedPreferences.Editor getSharedPreferenceEditor(Context context) {
        return getPreferences(context).edit();
    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("MyPref", 0);
    }

    public static void clearPreferneces(Context context) {
        getSharedPreferenceEditor(context).clear().commit();
    }

    public static void logout(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(context, gso);
        client.signOut();
        FirebaseAuth.getInstance().signOut();
    }
}
