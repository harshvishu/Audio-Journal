package com.brotherpowers.audiojournal.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.brotherpowers.audiojournal.Camera.CameraFragment;
import com.brotherpowers.audiojournal.Camera.PhotosFragment;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.TextEditor.TextEditorFragment;
import com.brotherpowers.audiojournal.Utils.Constants;

import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity
        implements PhotosFragment.OnFragmentInteractionListener,
        CameraFragment.OnFragmentInteractionListener {

    public static final int TaskNote = 0x1;
    public static final int TaskCamera = 0x2;
    public static final int TaskGallery = 0x3;

    long entry_id;

    public static void start(Activity parentActivity, long entry_id, int task_id) {
        Intent intent = new Intent(parentActivity, EditorActivity.class);
        intent.putExtra(Constants.KEYS.entry_id, entry_id);
        intent.putExtra(Constants.KEYS.task_id, task_id);
        parentActivity.startActivity(intent);
    }

    public static void start(Activity parentActivity, long entry_id, long attachment_id, int task_id) {
        Intent intent = new Intent(parentActivity, EditorActivity.class);
        intent.putExtra(Constants.KEYS.entry_id, entry_id);
        intent.putExtra(Constants.KEYS.task_id, task_id);
        parentActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        entry_id = getIntent().getLongExtra(Constants.KEYS.entry_id, -1);

        final int task_id = getIntent().getIntExtra(Constants.KEYS.task_id, 0);

        if (savedInstanceState == null) {

            Fragment fragment = null;
            switch (task_id) {
                case TaskNote:
                    fragment = TextEditorFragment.newInstance(entry_id);
                    break;
                case TaskCamera:
                    fragment = CameraFragment.newInstance(entry_id);
                    break;
                case TaskGallery:
                    Toast.makeText(this, "Pending", Toast.LENGTH_SHORT).show();
                    break;
            }
            if (fragment != null) {
                getSupportFragmentManager().
                        beginTransaction()
//                    .replace(R.id.container_text_editor, TextEditorFragment.newInstance(entry_id))
                        .replace(R.id.container_text_editor, fragment)
                        .commit();
            } else {
                throw new RuntimeException("Wrong task started");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
        Fragment fragment = PhotosFragment.newInstance(entry_id);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_text_editor, fragment)
                .addToBackStack("PhotosFragment")
                .commit();
    }

    @Override
    public void hideActionBar(boolean hide) {
        // TODO: 2/22/17 pending
    }

}
