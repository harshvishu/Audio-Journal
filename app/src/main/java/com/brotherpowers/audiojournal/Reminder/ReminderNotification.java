package com.brotherpowers.audiojournal.Reminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.brotherpowers.audiojournal.AudioRecorder.AudioPlayer;
import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.List;

import io.realm.Realm;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ReminderNotification extends Service {
    private static final String ACTION_PLAY = "com.brotherpowers.audiojournal.ReminderBroadcastReceiver.action.PLAY";
    private static final String EXTRA_JSON = "com.brotherpowers.audiojournal.ReminderBroadcastReceiver.extra.JSON";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionPlay(Context context, String json) {
        Intent intent = new Intent(context, ReminderNotification.class);
        intent.setAction(ACTION_PLAY);
        intent.putExtra(EXTRA_JSON, json);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return START_STICKY;

    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                final String json = intent.getStringExtra(EXTRA_JSON);
                handleActionPlay(json, intent);
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPlay(String json, Intent intent) {


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(this, "Alarm " + json, Toast.LENGTH_SHORT).show());

        //// TODO: 11/29/16 temp
        playSound(json, intent, this);
    }

    private Uri playSound(String data, Intent intent, Context context) {
        Uri uri = null;
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(data, JsonObject.class);
            if (json.has(Constants.KEYS.id)) {
                long id = json.get(Constants.KEYS.id).getAsLong();

                DataEntry dataEntry = Realm.getDefaultInstance().where(DataEntry.class)
                        .equalTo(Constants.KEYS.id, id).findFirst();

                assert dataEntry != null;

                // Remove the reminder time
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(r -> dataEntry.remindAt(null));

                Attachment attachment = dataEntry.audioFile();
                File file = attachment.file(context);

                uri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), file);

                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                NotificationManagerCompat manger = NotificationManagerCompat.from(this);
                Builder builder = new Builder(this).setColor(ContextCompat.getColor(this, R.color.colorAccent));
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.test_remote_view);

                builder.setSmallIcon(R.mipmap.ic_launcher)
//                        .setCustomContentView(remoteViews)
                        .setContentTitle(attachment.getFileName())
                        .setContentText("Playing")
                        .setAutoCancel(false);

                Notification notification = builder.build();

                AudioPlayer.sharedInstance.play(file, new AudioPlayer.PlaybackListener() {
                    @Override
                    public void onPlaybackStart(long id, int position) {
                        System.out.println("start");
                        manger.notify(0x99, notification);
                    }

                    @Override
                    public void onPlaybackStop(long id, int position) {
                        System.out.println("stop");
                        stopSelf();
                    }

                    @Override
                    public void playbackProgress(float progress, long id, int position) {
                        System.out.println("playbackProgress: " + progress);
                        manger.cancel(0x99);
                    }
                });


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uri;
    }


}
