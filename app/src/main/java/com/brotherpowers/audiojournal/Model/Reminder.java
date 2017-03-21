package com.brotherpowers.audiojournal.Model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.brotherpowers.audiojournal.Reminder.ReminderBroadcastReceiver;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by harsh_v on 2/22/17.
 */

public class Reminder extends RealmObject {
    private static final String TAG = "REMINDER";

    private Long remind_at = null;
    private boolean is_set = false;

    public Long getRemindAt() {
        return remind_at;
    }

    public Reminder setRemindAt(Long remind_at) {
        this.remind_at = remind_at;
        return this;
    }

    public boolean isSet() {
        return is_set;
    }

    public Reminder set(boolean is_set) {
        this.is_set = is_set;
        return this;
    }
}
