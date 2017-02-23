package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.View.ALViewHolder;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;


/**
 * Created by harsh_v on 2/22/17.
 */

public class ReminderAdapter extends RealmRecyclerViewAdapter<Reminder, ReminderAdapter.ViewHolderReminder> {


    public ReminderAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Reminder> data) {
        super(context, data, true);
    }

    @Override
    public ViewHolderReminder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_reminder, parent, false);

        return new ViewHolderReminder(view, (view1, position) -> {

        });
    }

    @Override
    public void onBindViewHolder(ViewHolderReminder holder, int position) {
        if (holder instanceof ViewHolderReminder) {

        }
    }

    static class ViewHolderReminder extends ALViewHolder implements View.OnClickListener {

        public ViewHolderReminder(View itemView, VhClick vhClick) {
            super(itemView, vhClick);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
