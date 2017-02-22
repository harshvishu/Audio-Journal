package com.brotherpowers.audiojournal.Main;

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
import com.brotherpowers.audiojournal.Camera.PhotosFragment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Records.RecordsFragment;
import com.brotherpowers.audiojournal.View.AJViewPager;
import com.viewpagerindicator.CirclePageIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
        implements AudioRecordingFragment.OnFragmentInteractionListener,
        RecordsFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener,
        PhotosFragment.OnFragmentInteractionListener {


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)
    AJViewPager mViewPager;

    @BindView(R.id.viewpager_indicator)
    CirclePageIndicator circlePageIndicator;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Set Page indicator
        circlePageIndicator.setViewPager(mViewPager);
        circlePageIndicator.setOnTouchListener((v, event) -> isRecording);


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

    /*
        * Recording Fragment Interface
        * */
    @Override
    public void onRecordingStateChange(AudioRecorder.STATE state) {
        isRecording = state == AudioRecorder.STATE.RECORDING;
        mViewPager.isPagingEnabled = !isRecording;
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
        EditorActivity.start(this, entry.getId(), EditorActivity.TaskCamera);
        return true;
    }

    /************************************************
     * {@link PhotosFragment}
     ************************************************/

    @Override
    public void openDetailedImageGallery(long entry_id, long attachment_id) {
        EditorActivity.start(this, entry_id, attachment_id, EditorActivity.TaskGallery);
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
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
