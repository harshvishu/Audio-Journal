package com.brotherpowers.audiojournal.Recorder;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
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
import com.brotherpowers.hvprogressview.ProgressView;
import com.brotherpowers.waveformview.Utils;
import com.brotherpowers.waveformview.WaveformView;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;


public class RecordingActivity extends AppCompatActivity {
    private static final String ARG_DATA_ENTRY_ID = "ARG_DATA_ENTRY_ID";
    private static final String ARG_FILE_ID = "ARG_FILE_ID";


    @BindView(R.id.progress_view)
    ProgressView progressView;

    @BindView(R.id.wave_view)
    WaveformView waveformView;


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
        PENDING, RECORDING, FINISHED;
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


//        long file_id = getIntent().getLongExtra(ARG_FILE_ID, -1L);
        long file_primary_key = System.currentTimeMillis();

        file = FileUtils.sharedInstance.getFile(FileUtils.Type.AUDIO, String.valueOf(file_primary_key), this);

        rFile = new RFile();
        rFile.setFileType(FileUtils.Type.AUDIO)
                .setId(file_primary_key)
                .setFileName(file.getName());


        buttonCapture.setOnClickListener(view -> {
            if (recordingState == STATE.PENDING) {
                startRecording();

            } else if (recordingState == STATE.RECORDING) {
                stopRecording();

            }
        });

    }

   /* private void stopPlaying() {
        try {
            recordingState = STATE.COMPLETED;

            mediaPlayer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
        buttonCapture.setImageResource(R.drawable.ic_play);
        labelMessage.setText("Play");

    }*/

    /*private void play() {
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

    }*/


    private void stopRecording() {

        recordingState = STATE.FINISHED;
        mediaRecorder.stop();
        onRecordingStop();
    }

    private void onRecordingStop() {
        mediaRecorder.release();
        mediaRecorder = null;

        buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
        buttonCapture.setImageResource(R.drawable.ic_play);

        dataEntry.setAudioFile(rFile);
        realm.executeTransaction(realm -> {
            realm.copyToRealmOrUpdate(dataEntry);
            realm.copyToRealmOrUpdate(rFile);
        });
    }

    private void startRecording() {

        recordingState = STATE.RECORDING;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSamplingRate(getMaxSampleRate());
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getPath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setMaxDuration(60_000); // ONE MINUTE


        try {

            mediaRecorder.prepare();

            buttonCapture.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_in_out));
            buttonCapture.setImageResource(R.drawable.ic_stop);


        } catch (IOException e) {
            Log.e("AudioRecorder", "prepare() failed");
        }

        mediaRecorder.setOnInfoListener((mediaRecorder1, what, extra) -> {

            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                recordingState = STATE.FINISHED;
                onRecordingStop();
            }

        });
        mediaRecorder.start();

        timerThread = new Thread() {
            @Override
            public void run() {
                super.run();
                final long startTime = System.currentTimeMillis();

                while (recordingState == STATE.RECORDING) {

                    float elapsedTime = (float) (System.currentTimeMillis() - startTime) / 1000f;
                    String text = getString(R.string.Sec, elapsedTime);
                    if (elapsedTime > 60f) {
                        text = getString(R.string.Min, 1);
                    }
                    float progress = 100 * elapsedTime / 60;
                    progressView.setProgress(progress, text);

                    try {
                        Thread.sleep(1000 / 30); //30 fps refresh rate
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        timerThread.start();

        samplingThread = new Thread() {
            @Override
            public void run() {
                super.run();

                while (recordingState == STATE.RECORDING) {

                    try {
                        samples = Utils.getAudioSamples(file);
                        waveformView.setSamples(samples, (int) file.length() / getMaxSampleRate());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000 / 12); //12 fps refresh rate
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        samplingThread.start();
    }

    short[] samples = new short[1024];
    private Thread timerThread;
    private Thread samplingThread;

    private int getMaxSampleRate() {
        android.media.AudioManager am = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            String value = am.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return 44100;
            }
        }
        return 44100;
    }


}
