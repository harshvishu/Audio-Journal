package com.brotherpowers.audiojournal.Main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.brotherpowers.audiojournal.Audios.RecordsFragment;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Recorder.AudioRecorder;
import com.brotherpowers.audiojournal.Recorder.RecordingFragment;
import com.brotherpowers.audiojournal.TextEditor.TextEditor;
import com.brotherpowers.audiojournal.View.AJViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;


public class MainActivity extends AppCompatActivity
        implements RecordingFragment.OnFragmentInteractionListener, RecordsFragment.OnFragmentInteractionListener {


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.container)
    AJViewPager mViewPager;

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private boolean isRecording;
    private Realm realm;

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
        tabLayout.setupWithViewPager(mViewPager);

        realm = Realm.getDefaultInstance();
        setupTabTitles(realm);

    }

    private void setupTabTitles(Realm realm) {

        // set up the tabs
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            tabLayout.getTabAt(i).setIcon(Section.at(i).drawable);
            tabLayout.getTabAt(i).setText(Section.at(i).title(realm));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    * Recording Fragment Interface
    * */
    @Override
    public void onRecordingStateChange(AudioRecorder.STATE state) {
        this.isRecording = state == AudioRecorder.STATE.RECORDING;
        mViewPager.isPagingEnabled = state != AudioRecorder.STATE.RECORDING;
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
        TextEditor.start(this);
        return true;
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
            return Section.at(position).fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return Section.values().length;
        }
    }

    @Override
    protected void onStop() {
        realm.removeAllChangeListeners();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Add change listeners to for tablayout
        realm.addChangeListener(element -> setupTabTitles(realm));
    }
}
