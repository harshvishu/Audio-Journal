package com.brotherpowers.audiojournal.Model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import com.brotherpowers.audiojournal.Reminder.ReminderBroadcastReceiver;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by harsh_v on 11/4/16.
 */

public class DataEntry extends RealmObject implements RealmDelegate {


    public String text_note = Constants.TEXT_NOTE_DEFAULT_JSON;    // Default JSON for text_note

    @PrimaryKey
    private long id;
    private Attachment audioFile;
    private String title;
    private long length;
    private RealmList<Attachment> attachments;
    private Date created_at = new Date();
    private Reminder reminder = new Reminder();

    public long getId() {
        return id;
    }

    public DataEntry setId(long id) {
        this.id = id;
        return this;
    }

    public Attachment audioFile() {
        return audioFile;
    }

    public DataEntry setAudioFile(Attachment audioFile) {
        this.audioFile = audioFile;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DataEntry setTitle(String title) {
        this.title = title;
        return this;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public RealmList<Attachment> getAttachments() {
        return attachments;
    }

    public DataEntry setAttachments(RealmList<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }


    public long getLength() {
        return length;
    }

    public DataEntry setLength(long length) {
        this.length = length;
        return this;
    }

    public Reminder getRemindAt() {
        return reminder;
    }

    /**
     * set reminder time & Activate
     */
    public void remindAt(Long remind_at) {
        this.reminder.setRemindAt(remind_at);
    }

    @Override
    public long nexID(Realm realm) {
        long newID = 0;
        try {
            Number maxID = realm.where(getClass()).max("id");
            newID = maxID == null ? 0 : maxID.longValue();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        newID += 1;
        return newID;
    }

    @Override
    public DataEntry generateId(Realm realm) {
        setId(nexID(realm));
        return this;
    }

    /**
     * Set a pending alarm
     */
    // TODO: 3/25/17 FIXME : IMPROVE,  Merge reminder into one function
    public void enableReminder(Context context) {
        final String TAG = "REMINDER";

        Reminder reminder = getRemindAt();
        Long remind_at = reminder.getRemindAt();
        if (remind_at == null) {
            return;
        }


        /* if (System.currentTimeMillis() > remind_at) {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(r -> dataEntry.remindAt(null));
            return;
        }*/

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(Constants.KEYS.id, getId());
        Gson gson = new Gson();

        intent.putExtra(Constants.KEYS.data, gson.toJson(jsonObject));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long time = remind_at;

        // TODO: 3/21/17 pending
//        long time = System.currentTimeMillis() + 3000;

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


        Log.v(TAG, ">>> Alarm set : " + DateUtils.formatDateTime(context, remind_at, DateUtils.FORMAT_NUMERIC_DATE) + "current time: " + new Date(System.currentTimeMillis()).toString());
    }

    /**
     * Cancel the existing alarm
     */
    // TODO: 3/25/17 FIXME : IMPROVE, Merge reminder into one function
    public void disableReminder(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel this alarm
        alarmManager.cancel(pendingIntent);
    }

}
