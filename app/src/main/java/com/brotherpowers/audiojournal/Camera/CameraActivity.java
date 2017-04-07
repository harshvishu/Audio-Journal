package com.brotherpowers.audiojournal.Camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.View.AJViewPager;
import com.crashlytics.android.Crashlytics;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends AppCompatActivity
        implements PhotosFragment.OnFragmentInteractionListener,
        CameraFragment.OnFragmentInteractionListener {

    /**
     * Call this to capture a new photo with {@link com.brotherpowers.audiojournal.Model.DataEntry}
     */
    public static void start(Activity parentActivity, long entry_id) {
        start(parentActivity, entry_id, null);
    }

    /**
     * Call this to override a photo with {@link com.brotherpowers.audiojournal.Model.Attachment}
     * <p>
     * This should not generally be used as every attachment is immutable
     */
    public static void start(Activity parentActivity, long entry_id, Long attachment_id) {
        Intent intent = new Intent(parentActivity, CameraActivity.class);
        intent.putExtra(Constants.KEYS.entry_id, entry_id);
        intent.putExtra(Constants.KEYS.attachment_id, attachment_id);
        parentActivity.startActivity(intent);
    }

    @BindView(R.id.container)
    AJViewPager _viewPager;

    @BindView(R.id.viewpager_indicator)
    CirclePageIndicator _circlePageIndicator;

    private CameraPagerAdapter cameraPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        setSupportActionBar(ButterKnife.findById(this, R.id.toolbar));  // Set the toolbar
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);          // Navigate home

        final long entry_id = getIntent().getLongExtra(Constants.KEYS.entry_id, Long.MIN_VALUE);
        cameraPagerAdapter = new CameraPagerAdapter(entry_id, getSupportFragmentManager());
        _viewPager.setAdapter(cameraPagerAdapter);

        // Set Page indicator
        _circlePageIndicator.setViewPager(_viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /************************************************
     * {@link PhotosFragment}
     ************************************************/

    @Override
    public void openDetailedImageGallery(long entry_id, long attachment_id) {

    }

    /************************************************
     * {@link CameraFragment}
     ************************************************/

    @Override
    public void openGalleryForDataEntry(long entry_id) {
        _viewPager.setCurrentItem(CameraPagerAdapter.Sections.pictures.index, true);
    }

    private static class CameraPagerAdapter extends FragmentPagerAdapter {

        enum Sections {
            camera(0), pictures(1);

            private final int index;

            Sections(int i) {
                this.index = i;
            }

            static Sections valueAt(int index) {
                switch (index) {
                    case 0:
                        return camera;
                    case 1:
                        return pictures;
                }
                return null;
            }
        }

        final long entry_id;

        CameraPagerAdapter(long entry_id, FragmentManager fm) {
            super(fm);
            this.entry_id = entry_id;
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public Fragment getItem(int position) {
            try {
                switch (Sections.valueAt(position)) {
                    case camera:
                        return CameraFragment.newInstance(entry_id);
                    case pictures:
                        return PhotosFragment.newInstance(entry_id);
                }
            } catch (NullPointerException e) {
                Crashlytics.logException(e);
            }
            return null;
        }

        @Override
        public int getCount() {
            return Sections.values().length;
        }
    }
}
