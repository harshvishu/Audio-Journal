package com.brotherpowers.audiojournal.Camera;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.Utils.DBHelper;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.bumptech.glide.Glide;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class ImageViewerActivity extends AppCompatActivity {

    public static void start(Activity parent, @Nullable Long entry_id, @NonNull Long attachment_id) {
        Intent intent = new Intent(parent, ImageViewerActivity.class);
        if (entry_id != null) {
            intent.putExtra(Constants.KEYS.entry_id, entry_id);
        }
        intent.putExtra(Constants.KEYS.attachment_id, attachment_id);
        parent.startActivity(intent);
    }

    @BindView(R.id.view_pager_image_viewer)
    ViewPager _ViewPager;

    @BindView(R.id.viewpager_indicator)
    CirclePageIndicator _CirclePageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);                                   // Set the toolbar
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);          // Navigate home


        final long entry_id = getIntent().getLongExtra(Constants.KEYS.entry_id, Long.MIN_VALUE);
        final long attachment_id = getIntent().getLongExtra(Constants.KEYS.attachment_id, Long.MIN_VALUE);


        Realm realm = Realm.getDefaultInstance();
        RealmResults<Attachment> results = null;

        int position = Integer.MIN_VALUE;
        if (entry_id < 0) {
            // This will give us only one result as id is a primary key constraint in Attachment
            results = DBHelper.filterFilesForType(FileUtils.Type.IMAGE,
                    RealmQuery.createQuery(realm, Attachment.class))
                    .findAllSorted("created_at", Sort.DESCENDING);
        } else {
            DataEntry entry = DBHelper.findEntryForId(entry_id, realm).findFirst();
            if (entry != null) {
                results = DBHelper.images(entry).findAllSorted("created_at", Sort.DESCENDING);
            }
        }

        // Find  the position of current attachment to be displayed
        if (results != null) {
            Attachment currentAttachment = results.where().equalTo(Constants.KEYS.id, attachment_id).findFirst();
            if (currentAttachment != null) {
                position = results.indexOf(currentAttachment);
            }
        }

        _ViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        _ViewPager.setAdapter(new ImageViewerAdapter(results, true));
        _CirclePageIndicator.setViewPager(_ViewPager);

        // if we have a positive position for attachment to be viewed
        // then move to that position
        if (position >= 0) {
            _ViewPager.setCurrentItem(position, false);
        }

        realm.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();  // Go back home
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class ImageViewerAdapter extends PagerAdapter {

        private final boolean hasAutoUpdates;
        private final OrderedRealmCollectionChangeListener listener;
        @Nullable
        private OrderedRealmCollection<Attachment> adapterData;

        private ImageViewerAdapter(@Nullable OrderedRealmCollection<Attachment> data, boolean hasAutoUpdates) {
            this.adapterData = data;
            this.hasAutoUpdates = hasAutoUpdates;
            this.listener = hasAutoUpdates ? createListener() : null;
        }

        private OrderedRealmCollectionChangeListener createListener() {
            return (collection, changeSet) -> {
                // null Changes means the async query returns the first time.
                if (changeSet == null) {
                    notifyDataSetChanged();
                }
            };
        }

        /**
         * Returns the item associated with the specified position.
         * Can return {@code null} if provided Realm instance by {@link OrderedRealmCollection} is closed.
         *
         * @param index index of the item.
         * @return the item at the specified position, {@code null} if adapter data is not valid.
         */
        @SuppressWarnings("WeakerAccess")
        @Nullable
        public Attachment getItem(int index) {
            //noinspection ConstantConditions
            return isDataValid() ? adapterData.get(index) : null;
        }

        /**
         * Returns data associated with this adapter.
         *
         * @return adapter data.
         */
        @Nullable
        public OrderedRealmCollection<Attachment> getData() {
            return adapterData;
        }

        /**
         * Updates the data associated to the Adapter. Useful when the query has been changed.
         * If the query does not change you might consider using the automaticUpdate feature.
         *
         * @param data the new {@link OrderedRealmCollection} to display.
         */
        @SuppressWarnings({"WeakerAccess", "unused"})
        public void updateData(@Nullable OrderedRealmCollection<Attachment> data) {
            if (hasAutoUpdates) {
                if (isDataValid()) {
                    //noinspection ConstantConditions
                    removeListener(adapterData);
                }
                if (data != null) {
                    addListener(data);
                }
            }

            this.adapterData = data;
            notifyDataSetChanged();
        }

        private void addListener(@NonNull OrderedRealmCollection<Attachment> data) {
            if (data instanceof RealmResults) {
                RealmResults<Attachment> results = (RealmResults<Attachment>) data;
                //noinspection unchecked
                results.addChangeListener(listener);
            } else if (data instanceof RealmList) {
                RealmList<Attachment> list = (RealmList<Attachment>) data;
                //noinspection unchecked
                list.addChangeListener(listener);
            } else {
                throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
            }
        }

        private void removeListener(@NonNull OrderedRealmCollection<Attachment> data) {
            if (data instanceof RealmResults) {
                RealmResults<Attachment> results = (RealmResults<Attachment>) data;
                //noinspection unchecked
                results.removeChangeListener(listener);
            } else if (data instanceof RealmList) {
                RealmList<Attachment> list = (RealmList<Attachment>) data;
                //noinspection unchecked
                list.removeChangeListener(listener);
            } else {
                throw new IllegalArgumentException("RealmCollection not supported: " + data.getClass());
            }
        }

        private boolean isDataValid() {
            return adapterData != null && adapterData.isValid();
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public int getCount() {
            return isDataValid() ? adapterData.size() : 0;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            Context context = collection.getContext();

            LayoutInflater inflater = LayoutInflater.from(context);
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.view_image_viewer_adapter, collection, false);

            ImageView imageView = ButterKnife.findById(layout, R.id.image_view_viewpager_adapter);

            Attachment attachment = getItem(position);

            if (attachment != null) {
                Glide.with(context)
                        .load(attachment.file(context))
                        .centerCrop()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_photo);
            }

            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class DepthPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

    private class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

}
