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

public class EditorActivity extends AppCompatActivity {

    public static final int TaskNote = 0x1;
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

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        entry_id = getIntent().getLongExtra(Constants.KEYS.entry_id, -1);

        final int task_id = getIntent().getIntExtra(Constants.KEYS.task_id, 0);

        if (savedInstanceState == null) {

            Fragment fragment = null;
            switch (task_id) {
                case TaskNote:
                    setTitle(R.string.title_text_editor);
                    fragment = TextEditorFragment.newInstance(entry_id);
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
}
