package com.brotherpowers.audiojournal.Audios;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.AudioRecorder.AudioPlayer;
import com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder;
import com.brotherpowers.audiojournal.Reminder.Alarm;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.View.RecyclerViewDecor;

import java.io.File;
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
    RecyclerView recyclerView;

    private RecordsAdapter recordsAdapter;
    private Realm realm;
    private OnFragmentInteractionListener interactionListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_recordings, container, false);
        ButterKnife.bind(this, view);

        realm = Realm.getDefaultInstance();

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
        recyclerView.addItemDecoration(new RecyclerViewDecor());
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
    }

    @Override
    public void addReminder(int position) {
        // TODO: 2/11/17 pending
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;

        realm.executeTransaction(r -> {
            entry.setRemindAt(System.currentTimeMillis() + 2000);
        });
        Alarm.set(getContext(), entry);
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
            ((RecordsAdapter.VHAudioRecord) holder).waveformView.setMarkerPosition((int) (progress * 1000) / AudioRecorder.SAMPLE_RATE);
        }
    }

    public interface OnFragmentInteractionListener {
        boolean startTextEditor(@NonNull DataEntry entry);
    }
}
