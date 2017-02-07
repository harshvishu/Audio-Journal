package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class Reminder extends WakefulBroadcastReceiver {
    private static final String TAG = "REMINDER";

    public Reminder() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String s = intent.getStringExtra("data");

        ReminderNotification.startActionPlay(context, s);

    }
}
