package com.brotherpowers.audiojournal.AudioRecorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by harsh_v on 11/23/16.
 */

public class AudioRecorder {
    public static final int DEFAULT_MAX_AUDIO_LENGTH = (int) (60_000 * 0.25); // 1 Minute
    public static final int SAMPLING_RATE = 44100; // Audio sampling rate 441.Khz
    private static final int BIT_RATE = 128000; // Audio encoding bit rate in bits per second.
    private File file;
    private final int maxDuration;
    private STATE recordingState;
    private MediaRecorder mediaRecorder;
    @Nullable
    private Listener listener;

    @SuppressWarnings("FieldCanBeLocal")
    private Thread timerThread;


    /**
     * @param context  {@link Context}
     * @param duration Maximum duration for recording
     */
    AudioRecorder(Context context, int duration) {
        maxDuration = duration;
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
        file = FileUtils.sharedInstance.getFile(FileUtils.Type.AUDIO, String.valueOf(System.currentTimeMillis()), context);

        recordingState = STATE.RECORDING;

        mediaRecorder = null;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioSamplingRate(SAMPLING_RATE);
        mediaRecorder.setAudioEncodingBitRate(BIT_RATE);
        mediaRecorder.setOutputFile(file.getPath());
        mediaRecorder.setMaxDuration(maxDuration); // ONE MINUTE
        mediaRecorder.setAudioChannels(1);

        try {
            mediaRecorder.prepare();
            if (listener != null) {
                listener.onRecordingStart();
            }

        } catch (IOException e) {
            Log.e("AudioRecorder", "prepare() failed");
        }

        mediaRecorder.setOnInfoListener((mr, what, extra) -> {

            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                onRecordingFinished();
            }

        });

        mediaRecorder.start();
        timerThread = createTimerThread(context);
        timerThread.start();

    }


    @NonNull
    private Thread createTimerThread(final Context context) {
        return new Thread() {
            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();

                while (recordingState == STATE.RECORDING) {

                    float elapsedTime = (float) (System.currentTimeMillis() - startTime);

                    String s = Extensions.millisToMSm(elapsedTime);

                    float progress = 100 * (elapsedTime / DEFAULT_MAX_AUDIO_LENGTH);

                    if (listener != null) {
                        listener.onProgress(progress, s);
                    }

                    try {
                        Thread.sleep(1000 / 60); //30 fps refresh rate
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

        if (listener != null) {
            listener.onRecordingStop(file);
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
    public enum STATE {
        PENDING, RECORDING, FINISHED
    }

    /**
     * Interface to interact with {@link AudioRecorder}
     */
    interface Listener {
        void onRecordingStart();

        void onRecordingStop(@Nullable File file);

        void onProgress(float progress, String text);

        void onSamples(short[] samples, int length);
    }
}
