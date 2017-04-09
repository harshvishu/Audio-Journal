package com.brotherpowers.audiojournal.Records;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
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
import com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder;
import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.DateTimePicker;
import com.brotherpowers.audiojournal.View.RecyclerViewDecorator;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordsFragment extends Fragment implements RecordsAdapter.Callback, AudioPlayer.PlaybackListener {


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private RecordsAdapter recordsAdapter;
    private Realm realm;
    private OnFragmentInteractionListener interactionListener;

    public RecordsFragment() {
        // Required empty public constructor
    }

    public static RecordsFragment newInstance() {

        Bundle args = new Bundle();

        RecordsFragment fragment = new RecordsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        realm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_recordings, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.interactionListener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(llm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recordsAdapter = new RecordsAdapter(getContext(), this, realm.where(DataEntry.class)
                .findAllAsync()
                .sort("created_at", Sort.DESCENDING)
        );
        recyclerView.setAdapter(recordsAdapter);

        Paint paintForSwipeView = new Paint();
        paintForSwipeView.setStyle(Paint.Style.FILL_AND_STROKE);
        paintForSwipeView.setStrokeCap(Paint.Cap.ROUND);

    }

    @Override
    public void actionDelete(int position) {
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;

        // Delete Entry
        realm.executeTransaction(r -> entry.empty(getContext()).deleteFromRealm());

    }

    @Override
    public void actionTextEditor(int position) {
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;

        boolean success = interactionListener.startTextEditor(entry);
        if (!success) {
            // TODO: 2/12/17 show alert
        }
    }

    @Override
    public void actionMore(int position) {
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;

        // TODO: 4/9/17 REMOVE this

    }

    @Override
    public void actionCamera(int position) {
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;

        interactionListener.startCamera(entry);
    }

    @Override
    public void addReminder(int position) {
        // TODO: 2/11/17 pending
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;

        DialogFragment fragment = ReminderDatePickerFragment.newInstance(entry.getId());
        fragment.show(getChildFragmentManager(), "DialogFragment");

        /*realm.executeTransaction(r -> {
            entry.remindAt(System.currentTimeMillis() + 2000);
        });
        Alarm.set(getContext(), entry);*/
    }

    @Override
    public void actionPlay(int position) {
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;

        long id = entry.getId();

        File file = entry.audioFile().file(getContext());
        if (file != null && file.exists()) {
            Observable.timer(100, TimeUnit.MILLISECONDS)
                    .subscribe(aLong -> {
                        AudioPlayer.sharedInstance.play(file, id, position, this);
                    });
        } else {
            System.out.println(">>>> AUDIO FILE IS NULL <<<<");
        }
    }

    /**
     * Configure the view when audio starts
     */
    @Override
    public void onPlaybackStart(long id, int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        if (holder instanceof RecordsAdapter.VHAudioRecord) {
            ((RecordsAdapter.VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_stop);
        }
    }

    @Override
    public void onPlaybackStop(long id, int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (holder instanceof RecordsAdapter.VHAudioRecord) {
                ((RecordsAdapter.VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_play);
                ((RecordsAdapter.VHAudioRecord) holder).waveformView.reset();
            }
        });
    }

    @Override
    public void playbackProgress(float progress, long id, int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        System.out.println(">>>>> progress " + progress);

        if (holder instanceof RecordsAdapter.VHAudioRecord) {
            ((RecordsAdapter.VHAudioRecord) holder).waveformView.setProgress(progress);
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
