package com.brotherpowers.audiojournal.Main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.brotherpowers.audiojournal.Camera.CameraFragment;
import com.brotherpowers.audiojournal.Camera.PhotosFragment;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.TextEditor.TextEditorFragment;
import com.brotherpowers.audiojournal.Utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity
        implements PhotosFragment.OnFragmentInteractionListener,
        CameraFragment.OnFragmentInteractionListener {

    public static final int TaskNote = 0x1;
    public static final int TaskCamera = 0x2;
    public static final int TaskGallery = 0x3;
    private static final long UI_ANIMATION_DELAY = 300;


    public static void start(Activity parentActivity, long entry_id, int task_id) {
        Intent intent = new Intent(parentActivity, EditorActivity.class);
        intent.putExtra(Constants.KEYS.entry_id, entry_id);
        intent.putExtra(Constants.KEYS.task_id, task_id);
        parentActivity.startActivity(intent);
    }

    public static void start(Activity parentActivity, long entry_id, long attachment_id, int task_id) {
        Intent intent = new Intent(parentActivity, EditorActivity.class);
        intent.putExtra(Constants.KEYS.entry_id, entry_id);
        intent.putExtra(Constants.KEYS.attachment_id, attachment_id);
        intent.putExtra(Constants.KEYS.task_id, task_id);
        parentActivity.startActivity(intent);
    }

    long entry_id;

    @BindView(R.id.content)
    FrameLayout contentView;

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
                        .replace(R.id.content, fragment)
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
                .replace(R.id.content, fragment)
                .addToBackStack("PhotosFragment")
                .commit();
    }


    @SuppressLint("InlinedApi")
    private void show() {

        contentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }


    private void hide() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        getWindow().getDecorView().setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    };

    @SuppressLint("InlinedApi")
    private final Runnable mHidePart2Runnable = () -> {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    };

    private final Handler mHideHandler = new Handler();
}
