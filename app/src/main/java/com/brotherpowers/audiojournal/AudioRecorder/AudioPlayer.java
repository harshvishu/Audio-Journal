package com.brotherpowers.audiojournal.AudioRecorder;

import android.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by harsh_v on 11/23/16.
 */

public class AudioPlayer {
    public static AudioPlayer sharedInstance = new AudioPlayer();

    private MediaPlayer mediaPlayer;
    private PlaybackListener playbackListener;
    private long id = -1L;
    private int position = -1;

    private CompositeDisposable _disposables;

    //Private constructor
    private AudioPlayer() {
        _disposables = new CompositeDisposable();
    }

    public void play(File file, PlaybackListener playbackListener) {
        play(file, 0x999L, 0x999, playbackListener);
    }

    public void play(File file, long id, int position, PlaybackListener playbackListener) {
        // stop and return
        if (this.id == id) {
            cancel();
            return;
        }
        if (isPlaying()) {
            cancel();
        }

        this.playbackListener = playbackListener;
        this.id = id;
        this.position = position;

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();

        try {

            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                    if (playbackListener != null) {
                        playbackListener.onPlaybackStart(id, position);
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    onStop();
                }
            });

            // Send progress details in an interval of second/3
            Disposable disposable = Observable.interval(1000 / 30, TimeUnit.MILLISECONDS, Schedulers.io())
                    .subscribeWith(new DisposableObserver<Long>() {
                        @Override
                        public void onNext(Long value) {
                            if (mediaPlayer != null && mediaPlayer.isPlaying() && playbackListener != null) {

                                playbackListener.playbackProgress((float) mediaPlayer.getCurrentPosition() / (float) mediaPlayer.getDuration(), id, position);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
            _disposables.add(disposable);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * call after stop
     */
    private void onStop() {
        _disposables.clear();
        mediaPlayer.release();
        mediaPlayer = null;

        if (playbackListener != null) {
            playbackListener.onPlaybackStop(id, position);
        }
        id = -1L;
        position = -1;
        playbackListener = null;
    }

    public void cancel() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            onStop();
        }

    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void setPlaybackListener(PlaybackListener playbackListener) {
        this.playbackListener = playbackListener;
    }

    public long getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public interface PlaybackListener {
        void onPlaybackStart(long id, int position);

        void onPlaybackStop(long id, int position);

        void playbackProgress(float progress, long id, int position);
    }
}
