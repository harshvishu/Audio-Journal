package com.brotherpowers.audiojournal.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.brotherpowers.audiojournal.Model.DataEntry;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Date;

import io.realm.Realm;

/**
 * Created by harsh_v on 11/29/16.
 */
public class Alarm {
    private static final String TAG = "ALARM";

    private Alarm() {
        // Private constructor to avoid conflict
    }

    /**
     * @param context {@link Context}
     */
    public static void set(Context context, DataEntry dataEntry) {
        Long remind_at = dataEntry.getRemindAt();
        if (remind_at == null) {
            return;
        }

        if (System.currentTimeMillis() > remind_at) {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(r -> dataEntry.setRemindAt(null));
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Reminder.class);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", dataEntry.getId());
        Gson gson = new Gson();

        intent.putExtra("data", gson.toJson(jsonObject));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        // Time at which
        long time = remind_at;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time, pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

            Log.v(TAG, ".... api 21 ...");

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            Log.v(TAG, ".... api 23 ...");
        } else {
            alarmManager.set(AlarmManager.RTC, time, pendingIntent);

            Log.v(TAG, ".... api all ...");
        }


        Log.v(TAG, "Alarm set : " + new Date(time).toString() + "current time: " + new Date(System.currentTimeMillis()).toString());
    }


}
