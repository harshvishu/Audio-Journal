package com.brotherpowers.audiojournal.Records;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.brotherpowers.audiojournal.AudioRecorder.AudioPlayer;
import com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.Model.Reminder;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.View.RecyclerViewDecorator;

import java.io.File;
import java.text.MessageFormat;
import java.util.Calendar;
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
        recyclerView.addItemDecoration(new RecyclerViewDecorator());
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

        realm.executeTransaction(r -> {
            RealmResults<DataEntry> dataEntries = r.where(DataEntry.class).equalTo("id", entry.getId()).findAll();
            if (!dataEntries.isEmpty()) {
                for (DataEntry dataEntry : dataEntries) {
                    dataEntry.deleteFromRealm();
                }
            }
        });

        // Remove Cached Samples
        recordsAdapter.cachedSamples.remove(entry.getId());
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
            entry.setRemindAt(System.currentTimeMillis() + 2000);
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
        if (holder instanceof RecordsAdapter.VHAudioRecord) {
            ((RecordsAdapter.VHAudioRecord) holder).waveformView.setMarkerPosition((int) (progress * 1000) / AudioRecorder.SAMPLING_RATE);
        }
    }

    public interface OnFragmentInteractionListener {
        boolean startTextEditor(@NonNull DataEntry entry);

        boolean startCamera(@NonNull DataEntry entry);
    }


    public final static class ReminderDatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        public static ReminderDatePickerFragment newInstance(long entry_id) {

            Bundle args = new Bundle();
            args.putLong(Constants.KEYS.entry_id, entry_id);

            ReminderDatePickerFragment fragment = new ReminderDatePickerFragment();
            fragment.setArguments(args);
            return fragment;
        }

        // Realm Instance
        private final Realm realm = Realm.getDefaultInstance();


        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final DataEntry entry = realm.where(DataEntry.class).equalTo(Constants.KEYS.id, getArguments().getLong(Constants.KEYS.entry_id)).findFirst();
            final Reminder reminder = entry.getRemindAt();

            Calendar calendar = Calendar.getInstance();

            final Long timeInMillis = reminder.getRemindAt();
            if (null != timeInMillis) {
                final Date date = new Date(timeInMillis);
                calendar.setTime(date);
            }

            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getContext(), this, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            return dialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            System.out.println(MessageFormat.format("year {0}, month {1}, day {2}", year, month, dayOfMonth));

            DialogFragment timeFragment = ReminderTimePickerDialog.newInstance(getArguments().getLong(Constants.KEYS.entry_id));
            timeFragment.show(getChildFragmentManager(), "DialogFragment");
        }
    }

    public static final class ReminderTimePickerDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        public static ReminderTimePickerDialog newInstance(long entry_id) {

            Bundle args = new Bundle();
            args.putLong(Constants.KEYS.entry_id, entry_id);
            args.putSerializable("date", Calendar.getInstance());

            ReminderTimePickerDialog fragment = new ReminderTimePickerDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar calendar = Calendar.getInstance();

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), this,
                    calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);


            return timePickerDialog;

        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        }
    }
}
