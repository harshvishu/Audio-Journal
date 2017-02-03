package com.brotherpowers.audiojournal.Recorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Realm.RFile;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.hvprogressview.ProgressView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.brotherpowers.audiojournal.Recorder.AudioRecorder.MAX_AUDIO_LENGTH;
import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;


public class RecordingActivity extends AppCompatActivity implements AudioRecorder.Listener {
    private static final String ARG_DATA_ENTRY_ID = "ARG_DATA_ENTRY_ID";
    private static final String ARG_FILE_ID = "ARG_FILE_ID";


    @BindView(R.id.progress_view)
    ProgressView progressView;


    /**
     * static function to recordingState this activity
     *
     * @param parentActivity
     */
    public static void start(Activity parentActivity) {
        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, Manifest.permission.RECORD_AUDIO)) {
                new AlertDialog.Builder(parentActivity)
                        .setTitle("Permission Denied")
                        .setMessage("App needs permission to record audio")
                        .setPositiveButton("Ok", (dialogInterface, i) -> {

                            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_REC_PERMISSION);

                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {

                        })
                        .show();
                return;
            }

            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_REC_PERMISSION);
            return;
        }

        Intent intent = new Intent(parentActivity, RecordingActivity.class);

        parentActivity.startActivity(intent);
    }

    @BindView(R.id.action_capture)
    FloatingActionButton buttonCapture;


    private static AudioRecorder audioRecorder;
    private AudioRecorder.STATE recordingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        ButterKnife.bind(this);
        //noinspection ConstantConditions
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            audioRecorder = new AudioRecorder(this, MAX_AUDIO_LENGTH);
        }

        audioRecorder.setListener(this);

        recordingState = audioRecorder.getRecordingState();
        if (recordingState == AudioRecorder.STATE.RECORDING) {
            buttonCapture.setImageResource(R.drawable.ic_stop);
        } else {
            buttonCapture.setImageResource(R.drawable.ic_mic);
        }


        buttonCapture.setOnClickListener(view -> {
            if (recordingState == AudioRecorder.STATE.PENDING) {
                audioRecorder.start(this);

            } else if (recordingState == AudioRecorder.STATE.RECORDING) {
                audioRecorder.stop();
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (audioRecorder.getRecordingState() == AudioRecorder.STATE.RECORDING) {
            audioRecorder.reset();
        }
        super.onBackPressed();
    }

    @Override
    public void onRecordingStart(AudioRecorder.STATE recordingState) {
        this.recordingState = recordingState;

        buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_open));
        buttonCapture.setImageResource(R.drawable.ic_stop);

    }

    @Override
    public void onRecordingStop(AudioRecorder.STATE recordingState, File file) {
        this.recordingState = recordingState;

        Realm realm = Realm.getDefaultInstance();


        DataEntry dataEntry = new DataEntry();
        dataEntry.generateId(realm);

        RFile rFile = new RFile();
        rFile.setFileType(FileUtils.Type.AUDIO)
                .setId(dataEntry.getId())
                .setFileName(file.getName());

        dataEntry.setAudioFile(rFile);
        realm.executeTransaction(r -> {
            r.copyToRealmOrUpdate(dataEntry);
            r.copyToRealmOrUpdate(rFile);
        });


        progressView.reset();
        this.recordingState = AudioRecorder.STATE.PENDING;
        buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fab_open));
        buttonCapture.setImageResource(R.drawable.ic_play);
//        finish();
    }

    @Override
    public void onProgress(float progress, String text) {
        progressView.setProgress(progress, text);
    }

    @Override
    public void onSamples(short[] samples, int length) {
//        waveformView.setSamples(samples, length);
    }
}
