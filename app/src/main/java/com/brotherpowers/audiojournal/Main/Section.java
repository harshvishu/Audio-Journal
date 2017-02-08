package com.brotherpowers.audiojournal.Main;

import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import com.brotherpowers.audiojournal.Audios.Records;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Recorder.RecordingFragment;

/**
 * Created by harsh_v on 2/8/17.
 */

public enum Section {

    recorder(0, R.drawable.ic_recording, RecordingFragment.newInstance()),
    records(1, R.drawable.ic_equalizer_white, Records.newInstance()),
    pictures(2, R.drawable.ic_photo_library_white, Records.newInstance()),
    reminders(3, R.drawable.ic_alarm_white, Records.newInstance());

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
        System.out.println(">>>>>>>>>>>>>>>>");
        this.position = position;
        this.drawable = drawable;
        this.fragment = fragment;
    }

    public static Section value(int position) {
        return ARRAY.get(position);
    }
}
