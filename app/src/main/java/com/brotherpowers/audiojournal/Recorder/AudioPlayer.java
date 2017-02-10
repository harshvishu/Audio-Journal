package com.brotherpowers.audiojournal.Recorder;

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
    private Listener listener;
    private long id = -1L;
    private int position = -1;

    private CompositeDisposable _disposables;

    //Private constructor
    private AudioPlayer() {
        _disposables = new CompositeDisposable();
    }

    public void play(File file, Listener listener) {
        play(file, 0x999L, 0x999, listener);
    }

    public void play(File file, long id, int position, Listener listener) {
        // stop and return
        if (this.id == id) {
            cancel();
            return;
        }
        if (isPlaying()) {
            cancel();
        }

        this.listener = listener;
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
                    if (listener != null) {
                        listener.onStart(id, position);
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    onStop();
                }
            });


            Disposable disposable = Observable.interval(1000 / 3, TimeUnit.MILLISECONDS, Schedulers.io())
                    .subscribeWith(new DisposableObserver<Long>() {
                        @Override
                        public void onNext(Long value) {
                            if (mediaPlayer != null && listener != null) {
                                listener.progress(mediaPlayer.getCurrentPosition(), id, position);

                            }
                        }

                        @Override
                        public void onError(Throwable e) {

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

        if (listener != null) {
            listener.onStop(id, position);
        }
        id = -1L;
        position = -1;
        listener = null;
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

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public long getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public interface Listener {
        void onStart(long id, int position);

        void onStop(long id, int position);

        void progress(float progress, long id, int position);
    }
}
