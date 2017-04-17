package com.brotherpowers.audiojournal.Records;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
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
import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

/**
 * Created by harsh_v on 4/10/17.
 */

class RecordsSectionedAdapter extends RealmRecyclerViewAdapter<DataEntry, VH> {
    private static final int SECTION_TYPE = 0;
    private static final int ITEM_TYPE = 1;

    private final LongSparseArray<short[]> cachedSamples;
    private final SparseArray<Section> sections;
    private final Context context;
    private Callback callback;

    RecordsSectionedAdapter(@NonNull Context context, RealmResults<DataEntry> data, Callback callback) {
        super(data, true);
        this.context = context;
        this.callback = callback;
        sections = new SparseArray<>();
        cachedSamples = new LongSparseArray<>();
    }


    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        VH holder;
        Context context = parent.getContext();

        if (viewType == SECTION_TYPE) {
            final View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_data_entry_section, parent, false);
            holder = new SectionViewHolder(view, null);
        } else if (viewType == ITEM_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_data_entry_item, parent, false);
            holder = new VHAudioRecord(view, (clickedView, adapterPosition) -> {

                // Adjust the position
                DataEntry entry = getItem(sectionedPositionToPosition(adapterPosition));

                switch (clickedView.getId()) {
                    case R.id.action_textEditor:
                        callback.actionTextEditor(entry, adapterPosition);
                        break;
                    case R.id.action_play:
                        callback.actionPlay(entry, adapterPosition);
                        break;
                    case R.id.action_camera:
                        callback.actionCamera(entry, adapterPosition);
                        break;
                    case R.id.action_reminder:
                        callback.addReminder(entry, adapterPosition);
                        break;
                    case R.id.action_more:
                        PopupMenu popupMenu = new PopupMenu(context, clickedView, Gravity.BOTTOM | Gravity.START);
                        popupMenu.inflate(R.menu.menu_records_more);
                        popupMenu.setOnMenuItemClickListener(item -> {
                            switch (item.getItemId()) {
                                case R.id.action_delete:
                                    // Call for delete
                                    callback.actionDelete(entry, adapterPosition);
                                    break;
                                case R.id.action_sync:
                                    break;
                            }
                            return false;
                        });
                        popupMenu.show();

                        break;
                }
            });
        } else {
            holder = null;
        }
        return holder;
    }


    @Override
    public void onBindViewHolder(VH holder, int position) {


        if (isSectionHeaderPosition(position)) {
            ((SectionViewHolder) holder)._title.setText(sections.get(position).getTitle());
        } else if (holder instanceof VHAudioRecord) {

            DataEntry entry = getItem(sectionedPositionToPosition(position));
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
                        ((VHAudioRecord) holder).waveformView.setSampleRate(AudioRecorder.SAMPLING_RATE);
                        ((VHAudioRecord) holder).waveformView.setChannels(1);
                        ((VHAudioRecord) holder).waveformView.setSamples(samples);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException ignored) {
                Crashlytics.logException(ignored);
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    boolean isValid() {
        return getData() != null && super.getItemCount() > 0;
    }

    @Override
    public int getItemCount() {
        return isValid() ? super.getItemCount() + sections.size() : 0;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public long getItemId(int position) {
        return isSectionHeaderPosition(position) ? Integer.MAX_VALUE - sections.indexOfKey(position) :
                sectionedPositionToPosition(position);
    }

    @Override
    public int getItemViewType(int position) {
        return isSectionHeaderPosition(position) ? SECTION_TYPE : ITEM_TYPE;
    }

    /**
     * returns TRUE is this position is occupied by Section
     */
    @SuppressWarnings("WeakerAccess")
    boolean isSectionHeaderPosition(int position) {
        return sections.get(position) != null;
    }

    @SuppressWarnings("WeakerAccess")
    int positionToSectionedPosition(int position) {
        int offset = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.valueAt(i).firstPosition > position) {
                break;
            }
            ++offset;
        }
        return position + offset;
    }

    /**
     * Convert {@link Section#sectionedPosition} #position
     */
    @SuppressWarnings("WeakerAccess")
    int sectionedPositionToPosition(int sectionedPosition) {
        if (isSectionHeaderPosition(sectionedPosition)) {
            return RecyclerView.NO_POSITION;
        }

        int offset = 0;
        for (int i = 0; i < sections.size(); i++) {
            if (sections.valueAt(i).sectionedPosition > sectionedPosition) {
                break;
            }
            --offset;
        }
        return sectionedPosition + offset;
    }

    public void setSections(@NonNull Section[] sections) {
        this.sections.clear();

        Arrays.sort(sections);

        int offset = 0; // offset positions for the headers we're adding
        for (Section section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            this.sections.append(section.sectionedPosition, section);
            ++offset;
        }
        notifyDataSetChanged();
    }

    static class Section implements Comparable<Section> {

        int firstPosition;
        int sectionedPosition;
        private CharSequence title;

        public Section(int firstPosition, CharSequence title) {
            this.firstPosition = firstPosition;
            this.title = title;
        }

        public final CharSequence getTitle() {
            return title;
        }

        @Override
        public final boolean equals(Object obj) {
            return obj instanceof Section && ((Section) obj).firstPosition == firstPosition && ((Section) obj).sectionedPosition == sectionedPosition && ((Section) obj).title.equals(title);
        }


        @Override
        public final int hashCode() {
            return firstPosition ^ ((sectionedPosition << Integer.SIZE / 2) | (sectionedPosition >> Integer.SIZE / 2));
        }

        @Override
        public final int compareTo(@NonNull Section o) {
            return firstPosition == o.firstPosition ? 0 : ((firstPosition < o.firstPosition) ? -1 : 1);
        }
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

    static class SectionViewHolder extends VH {
        @BindView(R.id.label_title)
        TextView _title;

        SectionViewHolder(View itemView, VhClick vhClick) {
            super(itemView, vhClick);
        }
    }


    public interface Callback {
        void actionDelete(DataEntry entry, int adapterPosition);

        void actionCamera(DataEntry entry, int adapterPosition);

        void addReminder(DataEntry entry, int adapterPosition);

        void actionPlay(DataEntry entry, int adapterPosition);

        void actionTextEditor(DataEntry entry, int adapterPosition);

        void actionMore(DataEntry entry, int adapterPosition);

    }
}
