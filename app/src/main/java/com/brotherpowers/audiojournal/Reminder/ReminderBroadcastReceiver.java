package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.brotherpowers.audiojournal.Utils.Constants;

public class ReminderBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "ReminderBroadcastReceiver";

    public ReminderBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        System.out.println(">>>>>> REMINDER BROADCAST RECEIVED");

        String s = intent.getStringExtra(Constants.KEYS.data);

        ReminderNotification.startActionPlay(context, s);

    }
}
