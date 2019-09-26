package dev.shantanu.com.chaton.uitls;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import dev.shantanu.com.chaton.data.entities.User;

public class Util {

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

    public static void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}
