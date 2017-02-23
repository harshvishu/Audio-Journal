package com.brotherpowers.audiojournal.Main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;

import com.brotherpowers.audiojournal.AudioRecorder.AudioRecordingFragment;
import com.brotherpowers.audiojournal.Camera.PhotosFragment;
import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Records.RecordsFragment;
import com.brotherpowers.audiojournal.Reminder.ReminderListFragment;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;

import java.util.Locale;

import io.realm.Realm;

/**
 * Created by harsh_v on 2/8/17.
 */

public enum FragmentSections {

    recorder(0, R.drawable.ic_recording, AudioRecordingFragment.newInstance(), AudioRecordingFragment.class),
    records(1, R.drawable.ic_equalizer_white, RecordsFragment.newInstance(), RecordsFragment.class),
    pictures(2, R.drawable.ic_photo_library_white, PhotosFragment.newInstance(null), PhotosFragment.class),
    reminders(3, R.drawable.ic_alarm_white, ReminderListFragment.newInstance(), ReminderListFragment.class);

    private static final SparseArray<FragmentSections> ARRAY = new SparseArray<>();

    static {
        for (FragmentSections sections : FragmentSections.values()) {
            ARRAY.append(sections.position, sections);
        }
    }

    public final int position;

    @DrawableRes
    public final int drawable;
    public final Fragment fragment;
    public final Class fragmentClass;

    FragmentSections(int position, @DrawableRes int drawable, Fragment fragment, Class fragmentClass) {
        this.position = position;
        this.drawable = drawable;
        this.fragment = fragment;
        this.fragmentClass = fragmentClass;
    }

    public static FragmentSections at(int position) {
        return ARRAY.get(position);
    }

    public String title(Realm realm) {
        String s = "";
        switch (this) {
            case recorder: {
                long length = realm.where(DataEntry.class).sum("length").longValue();
                s = Extensions.millisToHMS(length);
                break;
            }
            case records: {
                long totalNumberOfRecords = realm.where(DataEntry.class).count();
                s = String.format(Locale.getDefault(), "%2d", totalNumberOfRecords);
                break;
            }
            case pictures: {
                long totalNumberOfPictures = realm.where(Attachment.class).equalTo("fileType", FileUtils.Type.IMAGE.value).count();
                s = String.format(Locale.getDefault(), "%2d", totalNumberOfPictures);
                break;
            }
            case reminders: {
                long totalNumberOfReminders = realm.where(DataEntry.class).isNotNull("remind_at").count();
                s = String.format(Locale.getDefault(), "%2d", totalNumberOfReminders);
                break;
            }
        }
        return s;
    }

    public Drawable drawable(Context context) {
        return ContextCompat.getDrawable(context, drawable);
    }
}
