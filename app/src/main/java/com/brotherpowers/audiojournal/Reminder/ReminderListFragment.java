package com.brotherpowers.audiojournal.Reminder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.View.ContextRecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Display set reminders
 */
public class ReminderListFragment extends Fragment {

    public ReminderListFragment() {
        // Required empty public constructor
    }

    public static ReminderListFragment newInstance() {

        Bundle args = new Bundle();

        ReminderListFragment fragment = new ReminderListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.label_reminder_header_current_time)
    TextView _labelCurrentTime;

    @BindView(R.id.label_reminder_header_marker)
    TextView _LabelTimeMarker;

    @BindView(R.id.recycler_view)
    ContextRecyclerView _recyclerViewReminders;

    private ReminderAdapter reminderAdapter;
    private RealmResults<Reminder> results;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        /// Instantiate realm
        final Realm realm = Realm.getDefaultInstance();

        /// fetch results
        results = realm.where(Reminder.class).isNotNull("remind_at").findAllAsync();

        /// Initialize adapter
        reminderAdapter = new ReminderAdapter(getContext(), results);

        /// Close realm
        realm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_reminders, container, false);
        ButterKnife.bind(this, view);

        /// Show placeholder if list is empty
        results.addChangeListener(changeSet -> showPlaceholder(changeSet.isEmpty()));

        //Set Adapter
        _recyclerViewReminders.setAdapter(reminderAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setCurrentTime();
        // Set Receiver
        getContext().registerReceiver(timeChangeReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onResume() {
        /// Initially show/hide placeholder
        showPlaceholder(results.isEmpty());
        super.onResume();
    }

    /**
     * Show hide placeholder
     */
    public void showPlaceholder(boolean visible) {
        final View view = getView();
        if (view != null) {
            ButterKnife.findById(view, R.id.placeholder_container).setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private final TimeChangeReceiver timeChangeReceiver = new TimeChangeReceiver(_labelCurrentTime) {
        @Override
        public void onReceive(Context context, Intent intent) {
            setCurrentTime();
        }
    };

    /**
     * Set the current time into {@link #_labelCurrentTime}
     */
    private void setCurrentTime() {


//        _labelCurrentTime.setText(DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));

        Date now = new Date();
        /// format hour & minute
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm", Locale.getDefault());
        _labelCurrentTime.setText(sdf.format(now));

        /// format am/pm
        sdf.applyPattern("a");
        _LabelTimeMarker.setText(sdf.format(now));
    }

    @Override
    public void onDestroyView() {
        // Unregister Time Change Receiver
        getContext().unregisterReceiver(timeChangeReceiver);
        super.onDestroyView();
    }

    private static class LineDividerItemDecorations extends DividerItemDecoration {
        LineDividerItemDecorations(Context context, int orientation, int drawableRes) {
            super(context, orientation);
            setDrawable(ContextCompat.getDrawable(context, drawableRes));
        }

        LineDividerItemDecorations(Context context, int orientation) {
            super(context, orientation);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {


        }
    }

}
