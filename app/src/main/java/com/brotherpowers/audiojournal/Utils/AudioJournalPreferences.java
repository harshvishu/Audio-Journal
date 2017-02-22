package com.brotherpowers.audiojournal.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder;

/**
 * Created by harsh_v on 10/13/16.
 */
public class AudioJournalPreferences {
    private static final String prefToken = "token";
    private static final String prefUserId = "userId";
    private static final String prefUserId_old = "userId_old";
    private static final String ACTION_LOGOUT = "AudioJournalPreferences.logout";

    private static final String prefMaxRecordingDuration = "max_recording_duration";

    private static AudioJournalPreferences INSTANCE = null;
    private SharedPreferences sharedPreferences;

    // private constructor to avoid ambiguity
    public AudioJournalPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences("preferences_session_manager", Context.MODE_PRIVATE);
    }

   /* public static AudioJournalPreferences shared(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AudioJournalPreferences(context);
        }
        return INSTANCE;
    }*/

    public void didLogin(String token, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(prefToken, token);
        editor.putString(prefUserId, userId);
        editor.apply();

    }

    public void logOut(Context context) {
        sharedPreferences.edit()
                .remove(prefToken) // Remove old token
                .putString(prefUserId_old, getCurrentUserId()) // Save current user id as old id for future references
                .remove(prefUserId) // Remove old UserID
                .apply();

//        sharedPreferences.edit().clear().apply();
        Intent logOutIntent = new Intent(ACTION_LOGOUT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(logOutIntent);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.contains(prefToken);
    }

    public String getUserToken() {
        return sharedPreferences.getString(prefToken, "");
    }

    public String getCurrentUserId() {
        return sharedPreferences.getString(prefUserId, null);
    }

    public String getLastUserId() {
        return sharedPreferences.getString(prefUserId_old, null);
    }

    public int getMaxRecordingDuration() {
        return sharedPreferences.getInt(prefMaxRecordingDuration, AudioRecorder.DEFAULT_MAX_AUDIO_LENGTH);
    }

    public AudioJournalPreferences setMaxRecordingDuration(int duration) {
        sharedPreferences.edit().putInt(prefMaxRecordingDuration, duration).apply();
        return this;
    }
}
