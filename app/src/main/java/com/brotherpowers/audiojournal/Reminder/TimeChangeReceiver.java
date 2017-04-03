package com.brotherpowers.audiojournal.Reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Created by harsh_v on 3/25/17.
 */

public class TimeChangeReceiver extends BroadcastReceiver {
    private WeakReference<TextView> timeTextViewWeakReference;

    public TimeChangeReceiver() {
    }

    public TimeChangeReceiver(TextView labelCurrentTime) {
        timeTextViewWeakReference = new WeakReference<>(labelCurrentTime);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final TextView labelCurrentTime = timeTextViewWeakReference.get();
        if (labelCurrentTime != null) {
            labelCurrentTime.setText(DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        }
    }
}