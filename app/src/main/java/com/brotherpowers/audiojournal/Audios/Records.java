package com.brotherpowers.audiojournal.Audios;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.brotherpowers.audiojournal.Recorder.AudioPlayer;
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
public class Records extends Fragment implements RecordsAdapter.Callback, AudioPlayer.Listener {


    public Records() {
        // Required empty public constructor
    }

    public static Records newInstance() {

        Bundle args = new Bundle();

        Records fragment = new Records();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private RecordsAdapter recordsAdapter;
    private Realm realm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_recordings, container, false);
        ButterKnife.bind(this, view);

        realm = Realm.getDefaultInstance();

        return view;
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


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                new AlertDialog.Builder(getContext(), R.style.AppTheme_AlertDialog)
                        .setTitle("Delete")
                        .setMessage("This action will remove all data related to this entry")
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {

                            DataEntry entry = recordsAdapter.getItem(position);
                            assert entry != null;
                            actionDelete(position);

                        })
                        .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> recordsAdapter.notifyItemChanged(position))
                        .create().show();

            }


            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


                if (recordsAdapter.getActualSizeOfData() > 0) {

                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                        View itemView = viewHolder.itemView;
                        Drawable icon;

                        if (dX > 0) {
                            /*paintForSwipeView.setColor(ContextCompat.getColor(getContext(), R.color.colorSwipeRight));
                            RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());

                            c.drawRect(background, paintForSwipeView);

                            icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_mode_edit);
                            icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);


                            int size = Math.max(icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
                            int right = (int) background.right - size;
                            int left = right - size;
                            int top = (int) background.centerY() - size / 2;
                            int bottom = top + size;

                            icon.setBounds(left, top, right, bottom);
                            icon.draw(c);*/

                        } else {

                            int backgroundColor = ContextCompat.getColor(getContext(), R.color.colorSwipeLeft);
                            int marginTop = getResources().getDimensionPixelSize(R.dimen.spacing_8);
                            int marginBottom = getResources().getDimensionPixelSize(R.dimen.spacing_8);

                            paintForSwipeView.setColor(backgroundColor);
                            RectF background = new RectF((float) itemView.getRight() + dX,
                                    (float) itemView.getTop() + marginTop,
                                    (float) itemView.getRight(),
                                    (float) itemView.getBottom() - marginBottom);

                            c.drawRect(background, paintForSwipeView);

                            if (Math.abs(dX) > 10) {
                                icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_delete);

                                float alphaFactor = Math.abs(dX) / 100;
                                int iconColor = Extensions.adjustAlpha(Color.WHITE, alphaFactor, 0.1f, 1f);
                                icon.setColorFilter(iconColor, PorterDuff.Mode.SRC_ATOP);

                                int size = getResources().getDimensionPixelSize(R.dimen.button_32);
                                int left = itemView.getWidth() - 2 * size;
                                int top = (int) (background.centerY() - size / 2);
                                int right = left + size;
                                int bottom = top + size;

                                icon.setBounds(left, top, right, bottom);

                                if (right >= background.left) {
                                    icon.draw(c);
                                }
                            }
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });

//        itemTouchHelper.attachToRecyclerView(recyclerView);
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
                    System.out.println("item removed");
                }
            }
        });

        // Remove the cached Attachment adapter
        recordsAdapter.attachmentAdapter.remove(entry.getId());

        // Remove Cached Samples
        recordsAdapter.cachedSamples.remove(entry.getId());
    }


    @Override
    public void actionCamera(int position) {
        DataEntry entry = recordsAdapter.getItem(position);
        assert entry != null;
    }

    @Override
    public void addReminder(int position) {
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

    @Override
    public void onStart(long id, int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        if (holder instanceof RecordsAdapter.VHAudioRecord) {
            ((RecordsAdapter.VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_stop);
        }
    }

    @Override
    public void onStop(long id, int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        new Handler(Looper.getMainLooper()).post(() -> {
            if (holder instanceof RecordsAdapter.VHAudioRecord) {
                ((RecordsAdapter.VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_play);
                ((RecordsAdapter.VHAudioRecord) holder).waveformView.setMarkerPosition(0);
            }
        });
    }

    @Override
    public void progress(float progress, long id, int position) {
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        if (holder instanceof RecordsAdapter.VHAudioRecord) {
            ((RecordsAdapter.VHAudioRecord) holder).waveformView.setMarkerPosition((int) progress);
        }
    }
}
