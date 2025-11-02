package com.example.boocafe;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import java.util.HashMap;

public class SessionManager {
    private static final String PREF_NAME = "DASCAFE_PREFS";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // ✅ Save user session (setelah login berhasil)
    public void createLoginSession(int userId, String username, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);

        // ✅ PENTING: Gunakan commit() bukan apply() untuk memastikan data tersimpan
        editor.commit();
    }

    // ✅ Check if user is logged in
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // ✅ Get user data methods
    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "staff");
    }

    // ✅ Check if user is admin
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getRole());
    }

    // ✅ Get all user data sebagai HashMap (opsional)
    public HashMap<String, Object> getUserDetails() {
        HashMap<String, Object> user = new HashMap<>();
        user.put("isLoggedIn", pref.getBoolean(KEY_IS_LOGGED_IN, false));
        user.put("userId", pref.getInt(KEY_USER_ID, -1));
        user.put("username", pref.getString(KEY_USERNAME, ""));
        user.put("email", pref.getString(KEY_EMAIL, ""));
        user.put("role", pref.getString(KEY_ROLE, "staff"));
        return user;
    }

    // ✅ Logout user (clear session)
    public void logoutUser() {
        editor.clear();
        editor.commit();

        // Redirect to login
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // ✅ Check login status dan redirect jika perlu
    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}