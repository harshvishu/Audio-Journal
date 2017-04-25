package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.View.VH;

import java.text.MessageFormat;

import butterknife.BindView;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;


/**
 * Created by harsh_v on 2/22/17.
 */

class ReminderAdapter extends RealmRecyclerViewAdapter<Reminder, ReminderAdapter.ViewHolderReminder> {

    private final Context context;

    ReminderAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Reminder> data) {
        super(data, true);
        this.context = context;
    }

    @Override
    public ViewHolderReminder onCreateViewHolder(ViewGroup parent, int viewType) {
        View holderView = LayoutInflater.from(context).inflate(R.layout.viewholder_reminder, parent, false);

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

        /// set the time 09:23
        holder._labelReminderTime.setText(Extensions.SDF_HH_MM.format(time));

        /// Set AM/PM
        holder._labelReminderTimeMarker.setText(Extensions.SDF_AM_PM.format(time));


        String day = Extensions.SDF_EEE_D_MMM_YYYY.format(time);
        CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(time);

        holder._labelDescription.setText(MessageFormat.format("{0} remind {1}", day, String.valueOf(relativeTime).toLowerCase()));

        // If Reminder Set
        holder._switchReminder.setChecked(reminder.isEnable());

    }

    static class ViewHolderReminder extends VH implements View.OnClickListener {

        @BindView(R.id.label_reminder_time)
        TextView _labelReminderTime;

        @BindView(R.id.label_reminder_time_marker)
        TextView _labelReminderTimeMarker;

        @BindView(R.id.label_reminder_description)
        TextView _labelDescription;

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
