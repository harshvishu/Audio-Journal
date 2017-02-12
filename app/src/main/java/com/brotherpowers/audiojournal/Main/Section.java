package com.brotherpowers.audiojournal.Main;

import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.brotherpowers.audiojournal.Audios.RecordsFragment;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Realm.RFile;
import com.brotherpowers.audiojournal.Recorder.RecordingFragment;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;

import java.util.Locale;

import io.realm.Realm;

/**
 * Created by harsh_v on 2/8/17.
 */

public enum Section {

    recorder(0, R.drawable.ic_recording, RecordingFragment.newInstance()),
    records(1, R.drawable.ic_equalizer_white, RecordsFragment.newInstance()),
    pictures(2, R.drawable.ic_photo_library_white, RecordsFragment.newInstance()),
    reminders(3, R.drawable.ic_alarm_white, RecordsFragment.newInstance());

    private static final SparseArray<Section> ARRAY = new SparseArray<>();

    static {
        for (Section section : Section.values()) {
            ARRAY.append(section.position, section);
        }
    }

    public final int position;

    @DrawableRes
    public final int drawable;
    public final Fragment fragment;

    Section(int position, @DrawableRes int drawable, Fragment fragment) {
        this.position = position;
        this.drawable = drawable;
        this.fragment = fragment;
    }

    public static Section at(int position) {
        return ARRAY.get(position);
    }

    public String title(Realm realm) {
        String s = "";
        switch (this) {
            case recorder: {
                long length = realm.where(DataEntry.class).sum("length").longValue();
                s = Extensions.getFormattedAudioTime(length);
                break;
            }
            case records: {
                long totalNumberOfRecords = realm.where(DataEntry.class).count();
                s = String.format(Locale.getDefault(), "%2d", totalNumberOfRecords);
                break;
            }
            case pictures: {
                long totalNumberOfPictures = realm.where(RFile.class).equalTo("fileType", FileUtils.Type.IMAGE.value).count();
                s = String.format(Locale.getDefault(), "%2d", totalNumberOfPictures);
                if (totalNumberOfPictures > 0) {
                }
                break;
            }
            case reminders: {
                long totalNumberOfReminders = realm.where(DataEntry.class).isNotNull("remind_at").count();
                s = String.format(Locale.getDefault(), "%2d", totalNumberOfReminders);
                if (totalNumberOfReminders > 0) {
                }
                break;
            }
        }
        return s;
    }


}
