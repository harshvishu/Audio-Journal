package com.brotherpowers.audiojournal.Main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.brotherpowers.audiojournal.Camera.CameraFragment;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.TextEditor.TextEditorFragment;

import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity {

    public static final int TaskNote = 0x1;
    public static final int TaskCamera = 0x2;

    long entry_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        entry_id = getIntent().getLongExtra("id", -1);

        final int task_id = getIntent().getIntExtra("task_id", 0);

        if (savedInstanceState == null) {

            Fragment fragment = null;
            switch (task_id) {
                case TaskNote:
                    fragment = TextEditorFragment.newInstance(entry_id);
                    break;
                case TaskCamera:
                    fragment = CameraFragment.newInstance(entry_id);
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

    public static void start(Activity parentActivity, long entry_id, int task_id) {
        Intent intent = new Intent(parentActivity, EditorActivity.class);
        intent.putExtra("id", entry_id);
        intent.putExtra("task_id", task_id);
        parentActivity.startActivity(intent);
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
