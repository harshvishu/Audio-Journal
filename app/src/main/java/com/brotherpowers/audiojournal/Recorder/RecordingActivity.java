package com.brotherpowers.audiojournal.Recorder;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Realm.RFile;
import com.brotherpowers.audiojournal.Utils.FileUtils;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;


public class RecordingActivity extends AppCompatActivity {
    private static final String ARG_DATA_ENTRY_ID = "ARG_DATA_ENTRY_ID";
    private static final String ARG_FILE_ID = "ARG_FILE_ID";

    private boolean isNewRecording;


    /**
     * static function to recordingState this activity
     *
     * @param parentActivity
     * @param data_entry_id
     */
    public static void start(Activity parentActivity, @Nullable Long data_entry_id, @Nullable Long file_id) {
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
        if (data_entry_id == null) {
            data_entry_id = -1L;
            file_id = -1L;
        }

        if (file_id == null) {
            file_id = -1L;
        }

        intent.putExtra(ARG_DATA_ENTRY_ID, data_entry_id);
        intent.putExtra(ARG_FILE_ID, file_id);

        parentActivity.startActivity(intent);
    }

    @BindView(R.id.action_capture)
    FloatingActionButton buttonCapture;

    @BindView(R.id.label_message)
    AppCompatTextView labelMessage;


    private File file;
    private Realm realm;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    private STATE recordingState = STATE.PENDING;

    enum STATE {
        PENDING, RECORDING, PLAYING, COMPLETED
    }

    private DataEntry dataEntry;
    private RFile rFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        ButterKnife.bind(this);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        realm = Realm.getDefaultInstance();

        long data_entry_id = getIntent().getLongExtra(ARG_DATA_ENTRY_ID, -1L);
        // Check if deficiency is valid
        if (data_entry_id == -1L) {
            dataEntry = new DataEntry();
            // set Unique ID
            dataEntry.setId(System.currentTimeMillis());
        } else {
            DataEntry managedObject = realm.where(DataEntry.class)
                    .equalTo("id", data_entry_id)
                    .findFirst();

            dataEntry = realm.copyFromRealm(managedObject);
        }


        long file_id = getIntent().getLongExtra(ARG_FILE_ID, -1L);

        if (file_id == -1L) {

            long file_primary_key = System.currentTimeMillis();

            file = FileUtils.sharedInstance.getFile(FileUtils.Type.AUDIO, String.valueOf(file_primary_key), this);

            rFile = new RFile();
            rFile.setFileType(FileUtils.Type.AUDIO)
                    .setId(file_primary_key)
                    .setFileName(file.getName());

            isNewRecording = true;
        } else {

            RFile managedObject = realm.where(RFile.class)
                    .equalTo("id", file_id)
                    .findFirst();

            file = FileUtils.sharedInstance.getFile(FileUtils.Type.AUDIO, rFile.getFile_name(), this);

            rFile = realm.copyFromRealm(managedObject);

            isNewRecording = false;
        }


        buttonCapture.setOnClickListener(view -> {
            switch (recordingState) {
                case PENDING:
                    startRecording();
                    break;
                case RECORDING:
                    stopRecording();
                    break;

                case COMPLETED:
                    play();
                    break;
                case PLAYING:

                    stopPlaying();
                    break;
            }

        });

    }

    private void stopPlaying() {
        try {
            recordingState = STATE.COMPLETED;

            mediaPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
        buttonCapture.setImageResource(R.drawable.ic_play);
        labelMessage.setText("Play");

    }

    private void play() {
        mediaPlayer = new MediaPlayer();
        try {
            recordingState = STATE.PLAYING;

            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener((mediaPlayer1) -> {
                mediaPlayer1.release();
                recordingState = STATE.COMPLETED;
                buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
                buttonCapture.setImageResource(R.drawable.ic_play);
                labelMessage.setText("Play");
            });

            buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
            buttonCapture.setImageResource(R.drawable.ic_stop);
            labelMessage.setText("Playing ...");


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void stopRecording() {
        recordingState = STATE.COMPLETED;

        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
        buttonCapture.setImageResource(R.drawable.ic_play);
        labelMessage.setText("Play");

        if (isNewRecording) {
            dataEntry.setAudioFile(rFile);
        }

        realm.executeTransaction(realm -> {
            realm.copyToRealmOrUpdate(dataEntry);
            realm.copyToRealmOrUpdate(rFile);
        });

    }

    private void startRecording() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getPath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recordingState = STATE.RECORDING;

            mediaRecorder.prepare();

            buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
            buttonCapture.setImageResource(R.drawable.ic_stop);
            labelMessage.setText("Recording ...");

        } catch (IOException e) {
            Log.e("AudioRecorder", "prepare() failed");
        }

        mediaRecorder.start();
    }


}
