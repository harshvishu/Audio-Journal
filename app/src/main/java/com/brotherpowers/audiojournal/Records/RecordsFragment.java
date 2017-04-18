package com.brotherpowers.audiojournal.Records;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.brotherpowers.audiojournal.AudioRecorder.AudioPlayer;
import com.brotherpowers.audiojournal.Main.SectionedRealmAdapter;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.View.ContextRecyclerView;
import com.brotherpowers.audiojournal.View.DateTimePicker;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordsFragment extends Fragment implements RecordsSectionedAdapter.Callback, AudioPlayer.PlaybackListener {


    public RecordsFragment() {
        // Required empty public constructor
    }

    public static RecordsFragment newInstance() {

        Bundle args = new Bundle();

        RecordsFragment fragment = new RecordsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.recycler_view)
    ContextRecyclerView recyclerView;
    private Realm realm;
    private RecordsSectionedAdapter recordsAdapter;
    private OnFragmentInteractionListener interactionListener;
    private RealmResults<DataEntry> results;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        /// Initialize realm
        realm = Realm.getDefaultInstance();

        /// Fetch results
        results = realm.where(DataEntry.class)
                .findAllAsync()
                .sort("created_at", Sort.DESCENDING);

        /// Initialize adapter
        recordsAdapter = new RecordsSectionedAdapter(getContext(), results, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_recordings, container, false);
        ButterKnife.bind(this, view);

        /// Show placeholder if list is empty
        results.addChangeListener(changeSet -> showPlaceholder(changeSet.isEmpty()));

        /// Set adapter
        recyclerView.setAdapter(recordsAdapter);
        return view;
    }

    @Override
    public void onResume() {
        /// Initially show/hide placeholder
        showPlaceholder(results.isEmpty());

        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.interactionListener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public void actionDelete(DataEntry entry, int adapterPosition) {
        /// Delete Entry
        realm.executeTransaction(r -> entry.empty(getContext()).deleteFromRealm());
    }

    @Override
    public void actionTextEditor(DataEntry entry, int adapterPosition) {
        boolean success = interactionListener.startTextEditor(entry);
        if (!success) {
            // TODO: 2/12/17 show alert
        }
    }

    @Override
    public void actionMore(DataEntry entry, int adapterPosition) {


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

    @Override
    public void actionCamera(DataEntry entry, int adapterPosition) {

        interactionListener.startCamera(entry);
    }

    @Override
    public void addReminder(DataEntry entry, int adapterPosition) {


        DialogFragment fragment = ReminderDatePickerFragment.newInstance(entry.getId());
        fragment.show(getChildFragmentManager(), "DialogFragment");

    }

    @Override
    public void actionPlay(DataEntry entry, int adapterPosition) {
        System.out.println(">>>>> INSERT ADAPTER POSITION PLAY " + adapterPosition);

        long id = entry.getId();

        File file = entry.audioFile().file(getContext());
        if (file != null && file.exists()) {
            Observable.timer(100, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> AudioPlayer.sharedInstance.play(file, id, adapterPosition, this));
        } else {
            System.out.println(">>>> AUDIO FILE IS NULL <<<<");
        }
    }

    /**
     * Configure the view when audio starts
     */
    @Override
    public void onPlaybackStart(long id, int adapterPosition) {

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapterPosition);
        if (holder instanceof RecordsSectionedAdapter.VHAudioRecord) {
            ((RecordsSectionedAdapter.VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_stop);
        }
    }

    @Override
    public void onPlaybackStop(long id, int positiadapterPositionn) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(positiadapterPositionn);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (holder instanceof RecordsSectionedAdapter.VHAudioRecord) {
                ((RecordsSectionedAdapter.VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_play);
                ((RecordsSectionedAdapter.VHAudioRecord) holder).waveformView.reset();
            }
        });
    }

    @Override
    public void playbackProgress(float progress, long id, int adapterPosition) {

        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(adapterPosition);

        if (holder instanceof RecordsSectionedAdapter.VHAudioRecord) {
            ((RecordsSectionedAdapter.VHAudioRecord) holder).waveformView.setProgress(progress);
        }
    }

    public interface OnFragmentInteractionListener {
        boolean startTextEditor(@NonNull DataEntry entry);

        boolean startCamera(@NonNull DataEntry entry);
    }


    public final static class ReminderDatePickerFragment extends DialogFragment {
        public static ReminderDatePickerFragment newInstance(long entry_id) {

            Bundle args = new Bundle();
            args.putLong(Constants.KEYS.entry_id, entry_id);

            ReminderDatePickerFragment fragment = new ReminderDatePickerFragment();
            fragment.setArguments(args);
            return fragment;
        }

        // Realm Instance
        private final Realm realm = Realm.getDefaultInstance();
        private DateTimePicker dateTimePicker;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final DataEntry entry = realm.where(DataEntry.class).equalTo(Constants.KEYS.id, getArguments().getLong(Constants.KEYS.entry_id)).findFirst();
            final Reminder reminder = entry.getRemindAt();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null);

            dateTimePicker = (DateTimePicker) LayoutInflater.from(getContext()).inflate(R.layout.date_time_picker, null);
            builder.setView(dateTimePicker);

            final Long timeInMillis = reminder.getRemindAt();
            final long currentTime = System.currentTimeMillis();
            dateTimePicker.setMinDate(currentTime);

            if (null != timeInMillis && timeInMillis > currentTime) {
                final Date date = new Date(timeInMillis);
                dateTimePicker.setDate(date);

                // If we have a valid reminder then we can also cancel it
                builder.setNeutralButton(R.string.Remove, null);
            }

            return builder.create();

        }

        @Override
        public void onResume() {
            super.onResume();

            AlertDialog dialog = (AlertDialog) getDialog();
            if (dialog != null) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {

                    switch (dateTimePicker.getPicker()) {
                        case DateTimePicker.PICKER_DATE_PICKER:
                            dateTimePicker.showTimePicker();
                            break;
                        case DateTimePicker.PICKER_TIME_PICKER:

                            final DataEntry entry = realm.where(DataEntry.class)
                                    .equalTo(Constants.KEYS.id, getArguments().getLong(Constants.KEYS.entry_id))
                                    .findFirst();

                            //
                            System.out.println(">>>> DATE PICKER DATA: " + DateUtils.formatDateTime(getContext(), dateTimePicker.getTime().getTime(), DateUtils.FORMAT_NUMERIC_DATE));

                            // Persist
                            realm.executeTransaction(r -> entry.remindAt(dateTimePicker.getTime().getTime(), getContext()));

                            dialog.dismiss();
                            break;
                    }
                });

                // Dismiss on Cancel click
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> dialog.dismiss());

                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
                    final DataEntry entry = realm.where(DataEntry.class)
                            .equalTo(Constants.KEYS.id, getArguments().getLong(Constants.KEYS.entry_id))
                            .findFirst();

                    // Remove the existing reminder
                    realm.executeTransaction(r -> entry.remindAt(null, getContext()));

                    dialog.dismiss();
                });

            }
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onStart() {
            try {
                Dialog dialog = getDialog();
                if (dialog != null) {
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    dialog.setCanceledOnTouchOutside(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onStart();
        }

        @Override
        public void onPause() {
            // Stop any playback
            AudioPlayer.sharedInstance.cancel();
            super.onPause();
        }
    }

    private static class ItemDecoration extends RecyclerView.ItemDecoration {
        final Context context;

        private ItemDecoration(Context context) {
            this.context = context;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int childCount = parent.getChildCount();
            int childPosition = parent.getChildAdapterPosition(view);
            int itemCount = parent.getAdapter().getItemCount();

            if (childPosition == itemCount - 1) {

                outRect.bottom = context.getResources().getDimensionPixelSize(R.dimen.spacing_16);

            } else {
                super.getItemOffsets(outRect, view, parent, state);
            }
        }
    }
}
