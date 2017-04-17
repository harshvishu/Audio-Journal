package com.brotherpowers.audiojournal.Main;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder;
import com.brotherpowers.audiojournal.AudioRecorder.AudioRecordingFragment;
import com.brotherpowers.audiojournal.Camera.CameraActivity;
import com.brotherpowers.audiojournal.Camera.PhotosFragment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Records.RecordsFragment;
import com.brotherpowers.audiojournal.Reminder.ReminderListFragment;
import com.brotherpowers.audiojournal.View.AJViewPager;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;


public class MainActivity extends AppCompatActivity
        implements AudioRecordingFragment.OnFragmentInteractionListener,
        RecordsFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener,
        PhotosFragment.OnFragmentInteractionListener,
        ReminderListFragment.OnFragmentInteractionListener {


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)
    AJViewPager _ViewPager;

    @BindView(R.id.viewpager_indicator)
    CirclePageIndicator _CirclePageIndicator;

    private boolean isRecording;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        _ViewPager.setAdapter(sectionsPagerAdapter);
        _ViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                // Toggle background visibility
                boolean isSectionVisible = FragmentSections.values()[position].isVisible(realm);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // Set Page indicator
        _CirclePageIndicator.setViewPager(_ViewPager);
        _CirclePageIndicator.setOnTouchListener((v, event) -> isRecording);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        /// Close realm
        realm.close();
        super.onDestroy();
    }

    /**
     * Recording Fragment Interface
     */
    @Override
    public void onRecordingStateChange(AudioRecorder.STATE state) {
        isRecording = state == AudioRecorder.STATE.RECORDING;
        _ViewPager.isPagingEnabled = !isRecording;
        ((DrawerLayout) findViewById(R.id.drawer_layout))
                .setDrawerLockMode(isRecording ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED :
                        DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    /*
    * Records (List) Fragment Interface
    * */
    @Override
    public boolean startTextEditor(@NonNull DataEntry entry) {
        if (isRecording) {
            return false;
        }
        // Start Text Editor
        EditorActivity.start(this, entry.getId(), EditorActivity.TaskNote);
        return true;
    }

    @Override
    public boolean startCamera(@NonNull DataEntry entry) {
        if (isRecording) {
            return false;
        }
        // Start Text Editor
        CameraActivity.start(this, entry.getId());
        return true;
    }

    /************************************************
     * {@link PhotosFragment}
     ************************************************/


    @Override
    public void openDetailedImageGallery(long entry_id, long attachment_id) {
        // TODO: 4/9/17 Open Image in a horizontal view pager
    }

    /************************************************
     * {@link ReminderListFragment}
     ************************************************/
    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // Return fragment from position
            return FragmentSections.at(position).fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return FragmentSections.values().length;
        }
    }

}
