package com.brotherpowers.audiojournal.Recorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by harsh_v on 11/23/16.
 */

class AudioRecorder {
    static final int MAX_AUDIO_LENGTH = 60_000 * 2; // 1 Minute

    private final File file;
    private STATE recordingState;
    private MediaRecorder mediaRecorder;
    private final int sampleRate;
    private final int maxDuration;

    @Nullable
    private Listener listener;

    @SuppressWarnings("FieldCanBeLocal")
    private Thread timerThread;
//    private Thread samplingThread;
//    short[] samples = new short[1024];

    /**
     * @param context  {@link Context}
     * @param duration Maximum duration for recording
     */
    AudioRecorder(Context context, int duration) {
        file = FileUtils.sharedInstance.getFile(FileUtils.Type.AUDIO, String.valueOf(System.currentTimeMillis()), context);
        sampleRate = Extensions.getMaxSampleRate(context);
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

//        samplingThread = createSamplingThread();
//        samplingThread.start();
    }


    @NonNull
    private Thread createTimerThread(final Context context) {
        return new Thread() {
            @Override
            public void run() {
                super.run();
                final long startTime = System.currentTimeMillis();

                while (recordingState == STATE.RECORDING) {

                    float elapsedTime = (float) (System.currentTimeMillis() - startTime);
                    int min = (int) (elapsedTime / 60_000) % 60;
                    int sec = (int) (elapsedTime / 1000) % 60;
                    int mil = (int) (elapsedTime % 100);

                    String s = String.format(Locale.getDefault(), "%02d:%02d:%02d", min, sec, mil);

                   /* String text = context.getString(R.string.Sec, elapsedTime);
                    if (elapsedTime > 60f) {
                        text = context.getString(R.string.Min, 1);
                    }*/

                    float progress = 100 * (elapsedTime / MAX_AUDIO_LENGTH);

                    if (listener != null) {
                        listener.onProgress(progress, s);
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

    /*@NonNull
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
    }*/


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
            listener.onRecordingStop(recordingState, file);
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

        void onRecordingStop(STATE recordingState, File file);

        void onProgress(float progress, String text);

        void onSamples(short[] samples, int length);
    }
}
