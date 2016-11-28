package com.brotherpowers.audiojournal.Main;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Realm.RFile;
import com.brotherpowers.audiojournal.Recorder.AudioPlayer;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.ClickableViewHolder;
import com.brotherpowers.waveformview.Utils;
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

class DataEntryListAdapter extends RealmRecyclerViewAdapter<DataEntry, ClickableViewHolder> implements AudioPlayer.Listener {
    private final int VIEW_PLACEHOLDER = 0;
    private final int VIEW_ITEM = 1;
    private Callback callback;
    final LongSparseArray<ImageAdapter> attachmentAdapter;
    final LongSparseArray<short[]> cachedSamples;


    DataEntryListAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<DataEntry> data) {
        super(context, data, true);
        callback = (Callback) context;
        attachmentAdapter = new LongSparseArray<>();
        cachedSamples = new LongSparseArray<>();
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ClickableViewHolder clickableViewHolder;
        Context context = parent.getContext();

        if (viewType == VIEW_PLACEHOLDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.recyclerview_data_entry_placeholder, parent, false);
            clickableViewHolder = new ClickableViewHolderPlaceHolder(view, null);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_data_entry_item, parent, false);
            clickableViewHolder = new ClickableViewHolderItem(view, (holder_view, position) -> {
                DataEntry dataEntry = getItem(position);
                switch (holder_view.getId()) {
                    case R.id.action_delete:
//                        callback.actionDelete(getItem(position).getId());
                        /*realm.executeTransaction(r -> {
                            DataEntry managedObject = getItem(position);
                            if (managedObject != null) {
                                managedObject.deleteFromRealm();
                            }
                        });*/
                        // TODO: 11/23/16 replaced by swipe  listener

                        break;
                    case R.id.action_play:

                        if (dataEntry != null) {
                            try {

                                File file = dataEntry.audioFile().file(context);
                                if (file != null && file.exists()) {

                                    AudioPlayer.sharedInstance.play(file, dataEntry.getId(), position, this);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    case R.id.action_camera:
                        if (dataEntry != null) {
                            callback.actionCamera(dataEntry.getId(), position);

                        }
                        break;
                }
            });
        }
        return clickableViewHolder;
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        int viewType = getItemViewType(position);

        if (viewType == VIEW_PLACEHOLDER) {
            // Do Nothing
        } else {
            DataEntry entry = getItem(position);
            long id = entry.getId();

            if (entry == null || !entry.isLoaded() || !entry.isValid()) {
                return;
            }

            ClickableViewHolderItem viewHolderItem = (ClickableViewHolderItem) holder;

            String str = Extensions.formatHumanReadable.format(entry.getCreated_at());
            viewHolderItem.labelTitle.setText(str);

            if (entry.audioFile() == null) {
                viewHolderItem.buttonPlay.setImageResource(R.drawable.ic_mic);
            } else if (AudioPlayer.sharedInstance.getId() == entry.getId()) {
                viewHolderItem.buttonPlay.setImageResource(R.drawable.ic_stop);
            } else {
                viewHolderItem.buttonPlay.setImageResource(R.drawable.ic_play);
            }

            File audioFile = entry.audioFile().file(context);
            if (audioFile != null && audioFile.exists()) {

                try {
                    final short[] samples;
                    if (cachedSamples.get(id) != null) {
                        samples = cachedSamples.get(id);
                    } else {
                        samples = Utils.getAudioSamples(audioFile);
                    }

                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    Uri uri = FileProvider.getUriForFile(context, context.getString(R.string.file_provider_authority), audioFile);

                    mmr.setDataSource(context, uri);
                    int duration = Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    viewHolderItem.waveformView.setSamples(samples, duration);

                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else {
                System.out.println(">>>> null file");
            }

            RealmResults<RFile> images = entry.getAttachments()
                    .where()
                    .equalTo("fileType", FileUtils.Type.IMAGE.value)
                    .findAll();

            if (images.isEmpty()) {
                viewHolderItem.recyclerViewInternal.setVisibility(View.GONE);
            } else {
                final ImageAdapter imageAdapter;
                if (attachmentAdapter.get(entry.getId()) == null) {
                    imageAdapter = new ImageAdapter(context, images);
                    attachmentAdapter.append(entry.getId(), imageAdapter);
                } else {
                    imageAdapter = attachmentAdapter.get(entry.getId());
                    imageAdapter.updateData(images);
                }

                viewHolderItem.recyclerViewInternal.setVisibility(View.VISIBLE);
                viewHolderItem.recyclerViewInternal.setAdapter(imageAdapter);
                viewHolderItem.recyclerViewInternal.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
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
    int getActualSizeOfData() {
        return super.getItemCount();
    }

    @Override
    public void onStart(long id, int position) {
        notifyItemChanged(position);
    }

    @Override
    public void onStop(long id, int position) {
        notifyItemChanged(position);
    }

    @Override
    public void progress(float progress, long id, int position) {

    }

    static class ClickableViewHolderItem extends ClickableViewHolder implements View.OnClickListener {
        @BindView(R.id.label_text)
        AppCompatTextView labelTitle;

        @BindView(R.id.action_play)
        AppCompatImageButton buttonPlay;

        @BindView(R.id.action_camera)
        AppCompatImageButton buttonCamera;

        @BindView(R.id.wave_view)
        WaveformView waveformView;

        @BindView(R.id.recycler_view_internal)
        RecyclerView recyclerViewInternal;

        ClickableViewHolderItem(View itemView, VhClick vhClick) {
            super(itemView, vhClick);

            buttonPlay.setOnClickListener(this);
            buttonCamera.setOnClickListener(this);

        }


        @Override
        public void onClick(View view) {
            vhClick.onItemClick(view, getAdapterPosition());
        }
    }


    static class ClickableViewHolderPlaceHolder extends ClickableViewHolder {

        ClickableViewHolderPlaceHolder(View itemView, VhClick vhClick) {
            super(itemView, vhClick);
        }
    }

    private class ImageAdapter extends RealmRecyclerViewAdapter<RFile, ViewHolderImage> {
        public ImageAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<RFile> data) {
            super(context, data, true);
        }

        /*public ImageAdapter(RealmResults<RFile> images) {

        }*/

        @Override
        public ViewHolderImage onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.recycler_view_internal_image, parent, false);
            return new ViewHolderImage(view, (click_view, position) -> {
                Toast.makeText(context, "click image: " + position, Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onBindViewHolder(ViewHolderImage holder, int position) {
            RFile rFile = getData().get(position);

            Picasso.with(context)
                    .load(rFile.file(context))
                    .fit()
                    .into(holder.imageView);
        }

    }

    class ViewHolderImage extends ClickableViewHolder implements View.OnClickListener {
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

    interface Callback {
        void actionDelete(long id, int position);

        void actionCamera(long id, int position);
    }
}
