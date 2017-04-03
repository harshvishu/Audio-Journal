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
    private Long remind_at = null;
    private boolean is_set = false;

    public Long getRemindAt() {
        return remind_at;
    }

    Reminder setRemindAt(Long remind_at) {
        this.remind_at = remind_at;

        // If remind_at in NULL then disable alarm otherwise new reminder is set the it is enabled by default
        is_set = remind_at != null;
        return this;
    }

    public boolean isSet() {
        return is_set;
    }

    public boolean enable(boolean enable) {
        is_set = enable && remind_at != null;
        return is_set;
    }

}
