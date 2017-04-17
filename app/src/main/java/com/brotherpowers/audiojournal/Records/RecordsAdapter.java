package com.brotherpowers.audiojournal.Records;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.brotherpowers.audiojournal.AudioRecorder.AudioPlayer;
import com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.VH;
import com.brotherpowers.waveformview.WaveformView;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by harsh_v on 11/4/16.
 */

class RecordsAdapter extends RealmRecyclerViewAdapter<DataEntry, VH> {
    private static final int VIEW_ITEM = 1;

    private final Context context;
    private final LongSparseArray<short[]> cachedSamples;
    private final Callback callback;

    private RecordsAdapter(@NonNull Context context, Callback callback, @Nullable OrderedRealmCollection<DataEntry> data) {
        super(data, true);
        this.context = context;
        this.callback = callback;
        cachedSamples = new LongSparseArray<>();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        VH VH;
        Context context = parent.getContext();


        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_data_entry_item, parent, false);
        VH = new VHAudioRecord(view, (item_view, position) -> {

            switch (item_view.getId()) {
                case R.id.action_textEditor:
                    callback.actionTextEditor(position);
                    break;
                case R.id.action_play:
                    callback.actionPlay(position);
                    break;
                case R.id.action_camera:
                    callback.actionCamera(position);
                    break;
                case R.id.action_reminder:
                    callback.addReminder(position);
                    break;
                case R.id.action_more:
                    PopupMenu popupMenu = new PopupMenu(context, item_view, Gravity.BOTTOM | Gravity.START);
                    popupMenu.inflate(R.menu.menu_records_more);
                    popupMenu.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.action_delete:
                                // Call for delete
                                callback.actionDelete(position);
                                break;
                            case R.id.action_sync:
                                break;
                        }
                        return false;
                    });
                    popupMenu.show();

                    System.out.println(">>> GRAVITY :" + (80 | 20));

//                        callback.actionMore(position);
                    break;
            }
        });

        return VH;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {

        //While binding an audio record
        if (holder instanceof VHAudioRecord) {

            DataEntry entry = getItem(position);
            assert entry != null;

            long id = entry.getId();

            if (!entry.isLoaded() || !entry.isValid()) {
                return;
            }

            String str = Extensions.formatHumanReadable.format(entry.getCreated_at());
            ((VHAudioRecord) holder).labelTitle.setText(str);

            if (AudioPlayer.sharedInstance.getId() == entry.getId()) {
                ((VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_stop);
            } else {
                ((VHAudioRecord) holder).buttonPlay.setImageResource(R.drawable.ic_play);
            }

            try {
                File audioFile = entry.audioFile().file(context);
                if (audioFile != null && audioFile.exists()) {

                    try {
                        final short[] samples;
                        if (cachedSamples.get(id) != null) {
                            samples = cachedSamples.get(id);
                        } else {
                            samples = FileUtils.getAudioSamples(audioFile);
                            cachedSamples.append(id, samples);
                        }

                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        Uri uri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), audioFile);

                        mmr.setDataSource(context, uri);
//                        int duration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        ((VHAudioRecord) holder).waveformView.setSampleRate(AudioRecorder.SAMPLING_RATE);
                        ((VHAudioRecord) holder).waveformView.setChannels(1);
                        ((VHAudioRecord) holder).waveformView.setSamples(samples);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException ignored) {
            }
        }

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public long getItemId(int index) {
        return getItem(index).getId();
    }

    /**
     * @param position of the view
     * @return View type. Either 0 or 1 for Header and Item respectively
     * <p>
     * If we have no item display a placeholder
     * else we display normal item
     */
    @Override
    public int getItemViewType(int position) {
        return VIEW_ITEM;
    }


    public interface Callback {
        void actionDelete(int position);

        void actionCamera(int position);

        void addReminder(int position);

        void actionPlay(int position);

        void actionTextEditor(int position);

        void actionMore(int position);
    }

    static class VHAudioRecord extends VH {
        @BindView(R.id.label_title)
        TextView labelTitle;

        @BindView(R.id.action_play)
        ImageButton buttonPlay;

        @BindView(R.id.wave_view)
        WaveformView waveformView;


        VHAudioRecord(View itemView, VhClick vhClick) {
            super(itemView, vhClick);

        }

        @OnClick({R.id.action_camera, R.id.action_textEditor, R.id.action_play, R.id.action_reminder, R.id.action_more})
        void clickEvent(View view) {
            vhClick.onItemClick(view, getAdapterPosition());
        }
    }

}
