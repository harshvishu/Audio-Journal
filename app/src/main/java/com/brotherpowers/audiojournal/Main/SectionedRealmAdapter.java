package com.brotherpowers.audiojournal.Main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;

/**
 * Created by harsh_v on 4/10/17.
 */

public abstract class SectionedRealmAdapter<T extends RealmModel, VH extends RecyclerView.ViewHolder, S extends SectionedRealmAdapter.Section> extends RecyclerView.Adapter<VH> {

    protected static final int SECTION_TYPE = 0;
    protected static final int ITEM_TYPE = 1;

    protected final Context context;
    protected final SparseArray<S> sections;
    private RealmResults<T> results;
    protected List<T> copyResults;
    private OrderedRealmCollectionChangeListener<RealmResults<T>> listener;
    private final Realm realm;

    protected SectionedRealmAdapter(@NonNull Context context, RealmResults<T> results, boolean autoUpdate) {
        this.context = context;
        this.sections = new SparseArray<>();
        this.results = results;
        this.listener = (!autoUpdate) ? null : getRealmChangeListener();
        this.realm = Realm.getDefaultInstance();
        this.copyResults = new ArrayList<>();
        updateRealmResults(results);
    }

    /**
     * Update the RealmResults associated with the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature.
     *
     * @param queryResults the new RealmResults coming from the new query.
     */
    public void updateRealmResults(RealmResults<T> queryResults) {
        if (listener != null && results != null) {
            results.removeChangeListener(listener);
        }

        this.results = queryResults;
        if (listener != null && results != null) {
            results.addChangeListener(listener);
        }


        copyResults = realm.copyFromRealm(results);

        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return (isValid() ? results.size() + sections.size() : 0);
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

    protected T getItem(int position) {
        return results.get(position);
    }

    protected boolean isValid() {
        return results != null;
    }

    /**
     * returns TRUE is this position is occupied by Section
     */
    protected boolean isSectionHeaderPosition(int position) {
        return sections.get(position) != null;
    }

    protected int positionToSectionedPosition(int position) {
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
    protected int sectionedPositionToPosition(int sectionedPosition) {
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

    public void setSections(@NonNull S[] sections) {
        this.sections.clear();

        Arrays.sort(sections);

        int offset = 0; // offset positions for the headers we're adding
        for (S section : sections) {
            section.sectionedPosition = section.firstPosition + offset;
            this.sections.append(section.sectionedPosition, section);
            ++offset;
        }
        notifyDataSetChanged();
    }

    private OrderedRealmCollectionChangeListener<RealmResults<T>> getRealmChangeListener() {
        return (collection, changeSet) -> {
            for (OrderedCollectionChangeSet.Range change : changeSet.getChangeRanges()) {
                notifyItemRangeChanged(change.startIndex, change.length);
            }
            for (OrderedCollectionChangeSet.Range deletion : changeSet.getDeletionRanges()) {
                notifyItemRangeRemoved(deletion.startIndex, deletion.length);
            }
            for (OrderedCollectionChangeSet.Range insertion : changeSet.getInsertionRanges()) {
                notifyItemRangeInserted(insertion.startIndex, insertion.length);
            }
        };
        /*return new RealmChangeListener<RealmResults<T>>() {

            @Override
            public void onChange(RealmResults<T> results) {
                System.out.println(">>>>>>> INITIAL ");

                if (copyResults == null) {
                    notifyDataSetChanged();
                    copyResults = realm.copyFromRealm(results);
                } else {
                    *//*if (results.isEmpty()) {
                        copyResults = realm.copyFromRealm(results);
                        notifyDataSetChanged();
                        return;
                    }*//*

                    System.out.println(copyResults);
                    System.out.println("-------------");
                    System.out.println(results);

                    Patch<T> patch = DiffUtils.diff(copyResults, results);
                    List<Delta<T>> deltas = patch.getDeltas();
                    copyResults = realm.copyFromRealm(results);
                    if (!deltas.isEmpty()) {

                        for (final Delta delta : deltas) {

                            switch (delta.getType()) {
                                case DELETE:
                                    System.out.println(">>>>> DELETE");

                                    notifyItemRemoved(delta.getOriginal().getPosition());
                                    break;
                                case INSERT:
                                    System.out.println(">>>>> INSERT");
                                    notifyItemInserted(delta.getRevised().getPosition());
                                    *//*notifyItemRangeInserted(
                                            delta.getRevised().getPosition(),
                                            delta.getRevised().size());*//*
                                    break;
                                case CHANGE:
                                    System.out.println(">>>>> CHANGE");
                                    notifyItemChanged(delta.getRevised().getPosition());
                                   *//* notifyItemRangeChanged(
                                            delta.getRevised().getPosition(),
                                            delta.getRevised().size());*//*
                                    break;
                            }
                        }
                    }
                }
            }
        };*/
    }

    public static class Section implements Comparable<Section> {

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
}
