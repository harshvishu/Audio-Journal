package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.View.ALViewHolder;
import com.brotherpowers.waveformview.Utils;

import butterknife.BindView;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
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
        View holderView = inflater.inflate(R.layout.recyclerview_reminder, parent, false);

        return new ViewHolderReminder(holderView, (clickedView, position) -> {
            switch (clickedView.getId()) {
                // Clicked on reminder
                case R.id.switch_reminder:
                    if (clickedView instanceof Switch) {
                        final boolean newValue = ((Switch) clickedView).isChecked();

                        final Reminder reminder = getItem(position);
                        // TODO: 3/25/17 FIXME Remove Assertion
                        assert reminder != null;

                        final Realm realm = Realm.getDefaultInstance();
                        // Execute the transaction is async
                        realm.executeTransaction(r -> {
                            final boolean success = reminder.enable(newValue);
                            if (!success) {
                                ((Switch) clickedView).setChecked(false);
                            }
                        });

                    }
                    break;
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBindViewHolder(ViewHolderReminder holder, int position) {
        Reminder reminder = getItem(position);

        // Date & Time
        final long time = reminder.getRemindAt();
        final String formattedTime = Extensions.formatHumanReadable.format(time);

        System.out.println(">>>>>> TIME " + formattedTime);

        holder._labelReminderTime.setText(formattedTime);

        // If Reminder Set
        holder._switchReminder.setChecked(reminder.isSet());


    }

    static class ViewHolderReminder extends ALViewHolder implements View.OnClickListener {

        @BindView(R.id.label_reminder_time)
        TextView _labelReminderTime;

        @BindView(R.id.switch_reminder)
        Switch _switchReminder;

        ViewHolderReminder(View itemView, VhClick vhClick) {
            super(itemView, vhClick);

            // Click Listener for Switch
            _switchReminder.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            vhClick.onItemClick(v, getAdapterPosition());
        }
    }
}
