package com.brotherpowers.audiojournal.Camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;

import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends AppCompatActivity
        implements CameraFragment.OnFragmentInteractionListener {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        setSupportActionBar(ButterKnife.findById(this, R.id.toolbar));  // Set the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);          // Navigate home

        /// extract the id of data entry
        final long entry_id = getIntent().getLongExtra(Constants.KEYS.entry_id, Long.MIN_VALUE);

        /// transact with Camera fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, CameraFragment.newInstance(entry_id), "Camera")
                .commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /************************************************
     * {@link CameraFragment}
     ************************************************/

    @Override
    public void openGalleryForDataEntry(long entry_id) {

    }
}
