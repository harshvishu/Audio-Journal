package com.brotherpowers.audiojournal.Audios;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.AudioRecorder.AudioPlayer;
import com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.ALViewHolder;
import com.brotherpowers.waveformview.WaveformView;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by harsh_v on 11/4/16.
 */

class RecordsAdapter extends RealmRecyclerViewAdapter<DataEntry, ALViewHolder> {
    private final int VIEW_PLACEHOLDER = 0;
    private final int VIEW_ITEM = 1;

    private Callback callback;
    final LongSparseArray<AttachmentAdapter> attachmentAdapter;
    final LongSparseArray<short[]> cachedSamples;


    RecordsAdapter(@NonNull Context context, Callback callback, @Nullable OrderedRealmCollection<DataEntry> data) {
        super(context, data, true);
        this.callback = callback;
        attachmentAdapter = new LongSparseArray<>();
        cachedSamples = new LongSparseArray<>();
    }

    @Override
    public ALViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ALViewHolder ALViewHolder;
        Context context = parent.getContext();

        if (viewType == VIEW_PLACEHOLDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_data_entry_placeholder, parent, false);
            ALViewHolder = new VHPlaceHolder(view, null);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_data_entry_item, parent, false);
            ALViewHolder = new VHAudioRecord(view, (holder_view, position) -> {


                switch (holder_view.getId()) {
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
                }
            });
        }
        return ALViewHolder;
    }

    @Override
    public void onBindViewHolder(ALViewHolder holder, int position) {

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
                    int duration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    ((VHAudioRecord) holder).waveformView.setSampleRate(AudioRecorder.SAMPLE_RATE);
                    ((VHAudioRecord) holder).waveformView.setChannels(1);
                    ((VHAudioRecord) holder).waveformView.setSamples(samples);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            RealmResults<Attachment> images = entry.getAttachments()
                    .where()
                    .equalTo("fileType", FileUtils.Type.IMAGE.value)
                    .findAll();

            if (images.isEmpty()) {
                ((VHAudioRecord) holder).recyclerViewInternal.setVisibility(View.GONE);
            } else {
                final AttachmentAdapter attachmentAdapter;
                if (this.attachmentAdapter.get(entry.getId()) == null) {
                    attachmentAdapter = new AttachmentAdapter(context, images);
                    this.attachmentAdapter.append(entry.getId(), attachmentAdapter);
                } else {
                    attachmentAdapter = this.attachmentAdapter.get(entry.getId());
                    attachmentAdapter.updateData(images);
                }

                ((VHAudioRecord) holder).recyclerViewInternal.setVisibility(View.VISIBLE);
                ((VHAudioRecord) holder).recyclerViewInternal.setAdapter(attachmentAdapter);
                ((VHAudioRecord) holder).recyclerViewInternal.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            }
        }

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
        return getActualSizeOfData() == 0 ? VIEW_PLACEHOLDER : VIEW_ITEM;
    }

    @Override
    public int getItemCount() {
        int count = getActualSizeOfData();
        return count == 0 ? 1 : count;
    }

    /**
     * Untitled.png
     *
     * @return size of the actual data we are using for this adapter
     */
    public int getActualSizeOfData() {
        return super.getItemCount();
    }


    static class VHAudioRecord extends ALViewHolder implements View.OnClickListener {
        @BindView(R.id.label_text)
        TextView labelTitle;

        @BindView(R.id.action_play)
        ImageButton buttonPlay;

        @BindView(R.id.action_textEditor)
        ImageButton buttonTextEditor;

        @BindView(R.id.action_camera)
        ImageButton buttonCamera;

        @BindView(R.id.action_reminder)
        ImageButton buttonReminder;

        @BindView(R.id.wave_view)
        WaveformView waveformView;

        @BindView(R.id.recycler_view_internal)
        RecyclerView recyclerViewInternal;

        VHAudioRecord(View itemView, VhClick vhClick) {
            super(itemView, vhClick);

            buttonTextEditor.setOnClickListener(this);
            buttonPlay.setOnClickListener(this);
            buttonCamera.setOnClickListener(this);
            buttonReminder.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            vhClick.onItemClick(view, getAdapterPosition());
        }
    }


    static class VHPlaceHolder extends ALViewHolder {

        VHPlaceHolder(View itemView, VhClick vhClick) {
            super(itemView, vhClick);
        }
    }

    private class AttachmentAdapter extends RealmRecyclerViewAdapter<Attachment, ViewHolderImage> {
        AttachmentAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<Attachment> data) {
            super(context, data, true);
        }

        @Override
        public ViewHolderImage onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.recycler_view_internal_image, parent, false);
            return new ViewHolderImage(view, (click_view, position) -> {
                Toast.makeText(context, "click image: " + position, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onBindViewHolder(ViewHolderImage holder, int position) {
            Attachment attachment = getData().get(position);
            Picasso.with(context)
                    .load(attachment.file(context))
                    .fit()
                    .into(holder.imageView);
        }

    }

    class ViewHolderImage extends ALViewHolder implements View.OnClickListener {
        @BindView(R.id.image_view_internal)
        AppCompatImageView imageView;


        ViewHolderImage(View itemView, VhClick vhClick) {
            super(itemView, vhClick);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            vhClick.onItemClick(view, getAdapterPosition());
        }
    }

    public interface Callback {
        void actionDelete(int position);

        void actionCamera(int position);

        void addReminder(int position);

        void actionPlay(int position);

        void actionTextEditor(int position);
    }
}
