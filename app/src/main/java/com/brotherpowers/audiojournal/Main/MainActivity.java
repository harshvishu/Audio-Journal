package com.brotherpowers.audiojournal.Main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Recorder.AudioPlayer;
import com.brotherpowers.audiojournal.Recorder.RecordingActivity;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.image)
    AppCompatImageView backImageView;

    private ListRecordingFragment listRecordingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(getContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        getSupportActionBar().setIcon(R.mipmap.ic_launcher);


        listRecordingFragment = (ListRecordingFragment) getSupportFragmentManager().findFragmentById(R.id.content_main);

        if (listRecordingFragment == null) {
            listRecordingFragment = ListRecordingFragment.newInstance();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, listRecordingFragment).commit();

        Picasso.with(getContext())
                .load(R.drawable.b_2)
                .fit()
                .into(backImageView);

    }

    @OnClick(R.id.fab)
    void record() {
        Log.v("MAIN","recorder clicked");
        RecordingActivity.start(this);
    }

    @NonNull
    private MainActivity getContext() {
        return MainActivity.this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_REC_PERMISSION: {
                boolean global_result = true;

                if (grantResults.length > 0) {

                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            System.out.println("permission granted");
                            global_result = false;
                        } else {
                            System.out.println("permission not granted");
                        }
                    }

                    /**
                     * If we have all the permissions
                     */
                    if (global_result) {
                        Toast.makeText(getContext(), "You can record audio", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "App doesn't have all the required permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }


    @Override
    public void onBackPressed() {
        if (AudioPlayer.sharedInstance.isPlaying()) {
            AudioPlayer.sharedInstance.cancel();
        } else {
            super.onBackPressed();
        }
    }


}
