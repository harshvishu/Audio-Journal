package com.brotherpowers.audiojournal.TextEditor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.brotherpowers.audiojournal.R;

import butterknife.ButterKnife;

public class TextEditorActivity extends AppCompatActivity {


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

        if (savedInstanceState == null) {

            getSupportFragmentManager().
                    beginTransaction()
                    .replace(R.id.container_text_editor, TextEditorFragment.newInstance(entry_id))
                    .commit();
        }
    }

    public static void start(Activity parentActivity, long entry_id) {
        Intent intent = new Intent(parentActivity, TextEditorActivity.class);
        intent.putExtra("id", entry_id);
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
