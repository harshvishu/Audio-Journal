package com.brotherpowers.audiojournal.Recorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Realm.RFile;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.waveformview.Utils;

import java.io.File;
import java.io.IOException;

import io.realm.Realm;

/**
 * Created by harsh_v on 11/23/16.
 */

class AudioRecorder {
    static final int MAX_AUDIO_LENGTH = 60_000; // 1 Minute

    private final DataEntry dataEntry;
    private final File file;
    private final RFile rFile;
    private STATE recordingState;
    private MediaRecorder mediaRecorder;
    private final int sampleRate;
    private final int maxDuration;

    @Nullable
    private Listener listener;

    @SuppressWarnings("FieldCanBeLocal")
    private Thread timerThread;
    @SuppressWarnings("FieldCanBeLocal")
    private Thread samplingThread;
    @SuppressWarnings("WeakerAccess")
    short[] samples = new short[1024];

    /**
     * @param context  {@link Context}
     * @param duration Maximum duration for recording
     */
    AudioRecorder(Context context, int duration) {
        this.dataEntry = new DataEntry();
        dataEntry.setId(System.currentTimeMillis());
        file = FileUtils.sharedInstance.getFile(FileUtils.Type.AUDIO, String.valueOf(dataEntry.getId()), context);
        sampleRate = Extensions.getMaxSampleRate(context);
        maxDuration = duration;

        rFile = new RFile();
        rFile.setFileType(FileUtils.Type.AUDIO)
                .setId(dataEntry.getId())
                .setFileName(file.getName());

        recordingState = STATE.PENDING;
    }

    /**
     * set the listener
     *
     * @param listener {@link Listener}
     */
    void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    /**
     * start recording
     *
     * @param context {@link Context}
     */
    void start(Context context) {
        recordingState = STATE.RECORDING;

        mediaRecorder = null;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSamplingRate(sampleRate);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(file.getPath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setMaxDuration(maxDuration); // ONE MINUTE

        try {
            mediaRecorder.prepare();
            if (listener != null) {
                listener.onRecordingStart(recordingState);
            }

        } catch (IOException e) {
            Log.e("AudioRecorder", "prepare() failed");
        }

        mediaRecorder.setOnInfoListener((mediaRecorder1, what, extra) -> {

            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                onRecordingFinished();
            }

        });

        mediaRecorder.start();

        timerThread = createTimerThread(context);
        timerThread.start();

        samplingThread = createSamplingThread();
        samplingThread.start();
    }


    @NonNull
    private Thread createTimerThread(final Context context) {
        return new Thread() {
            @Override
            public void run() {
                super.run();
                final long startTime = System.currentTimeMillis();

                while (recordingState == STATE.RECORDING) {

                    float elapsedTime = (float) (System.currentTimeMillis() - startTime) / 1000f;
                    String text = context.getString(R.string.Sec, elapsedTime);
                    if (elapsedTime > 60f) {
                        text = context.getString(R.string.Min, 1);
                    }
                    float progress = 100 * elapsedTime / 60;

                    if (listener != null) {
                        listener.onProgress(progress, text);
                    }

                    try {
                        Thread.sleep(1000 / 30); //30 fps refresh rate
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
    }

    @NonNull
    private Thread createSamplingThread() {
        return new Thread() {
            @Override
            public void run() {
                super.run();

                while (recordingState == STATE.RECORDING) {

                    try {
                        samples = Utils.getAudioSamples(file);

                        if (listener != null) {
                            listener.onSamples(samples, (int) file.length() / sampleRate);
                        }

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
    }


    /**
     * Stop  recording
     */
    void stop() {
        mediaRecorder.stop();
        onRecordingFinished();
    }

    /**
     * Called when recording is finished
     */
    private void onRecordingFinished() {
        recordingState = STATE.FINISHED;
        try {
            mediaRecorder.release();
            mediaRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Realm realm = Realm.getDefaultInstance();
        dataEntry.setAudioFile(rFile);
        realm.executeTransaction(r -> {
            r.copyToRealmOrUpdate(dataEntry);
            r.copyToRealmOrUpdate(rFile);
        });

        if (listener != null) {
            listener.onRecordingStop(recordingState);
        }
    }

    STATE getRecordingState() {
        return recordingState;
    }

    /**
     * Reset the media recorder in case we left with completing
     */
    void reset() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.release();
                mediaRecorder = null;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Enum to describe recording states
     */
    enum STATE {
        PENDING, RECORDING, FINISHED
    }

    /**
     * Interface to interact with {@link AudioRecorder}
     */
    interface Listener {
        void onRecordingStart(STATE recordingState);

        void onRecordingStop(STATE recordingState);

        void onProgress(float progress, String text);

        void onSamples(short[] samples, int length);
    }
}
