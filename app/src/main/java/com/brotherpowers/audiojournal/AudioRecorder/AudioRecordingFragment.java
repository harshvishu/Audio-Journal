package com.brotherpowers.audiojournal.AudioRecorder;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Realm.Attachment;
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.PermissionRequestFragment;
import com.brotherpowers.hvprogressview.ProgressView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.brotherpowers.audiojournal.AudioRecorder.AudioRecorder.MAX_AUDIO_LENGTH;
import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;

/**
 * A simple {@link Fragment} subclass.
 */
public class AudioRecordingFragment extends Fragment implements AudioRecorder.Listener {
    private static final String FRAGMENT_DIALOG = "dialog";

    @BindView(R.id.progress_view)
    ProgressView progressView;

    @BindView(R.id.action_capture)
    FloatingActionButton buttonRecord;

    public AudioRecordingFragment() {
        // Required empty public constructor
    }

    public static AudioRecordingFragment newInstance() {

        Bundle args = new Bundle();

        AudioRecordingFragment fragment = new AudioRecordingFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private static AudioRecorder audioRecorder;
    private AudioRecorder.STATE recordingState;
    private OnFragmentInteractionListener interactionListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        audioRecorder = new AudioRecorder(getContext(), MAX_AUDIO_LENGTH);
        audioRecorder.setListener(this);
        recordingState = audioRecorder.getRecordingState();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Connect the interface with activity
        this.interactionListener = (OnFragmentInteractionListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recording, container, false);
        ButterKnife.bind(this, view);


        // Set the recording button with recording state
        buttonRecord.setImageResource(recordingState == AudioRecorder.STATE.RECORDING
                ? R.drawable.ic_stop : R.drawable.ic_mic);

        buttonRecord.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {

                if (recordingState == AudioRecorder.STATE.PENDING) {
                    // Stop any audio player
                    AudioPlayer.sharedInstance.cancel();
                    audioRecorder.start(getContext());
                    buttonRecord.setEnabled(false);     // Disable button click

                } else if (recordingState == AudioRecorder.STATE.RECORDING) {
                    audioRecorder.stop();
                    buttonRecord.setEnabled(false);     // Disable button click
                }

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                PermissionRequestFragment
                        .newInstance(R.string.record_permission_confirmation,
                                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQ_REC_PERMISSION,
                                R.string.record_permission_not_granted)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_REC_PERMISSION);
            }

        });
        return view;
    }


    @Override
    public void onRecordingStart() {
        this.recordingState = AudioRecorder.STATE.RECORDING;

        // Interface
        interactionListener.onRecordingStateChange(recordingState);

        buttonRecord.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
        buttonRecord.setImageResource(R.drawable.ic_stop);
        System.out.println(">>>>>> enable button");

        new Handler(Looper.getMainLooper()).post(() -> {
            buttonRecord.setEnabled(true);     // Enable button click
        });
    }


    @Override
    public void onRecordingStop(File file) {
        this.recordingState = AudioRecorder.STATE.FINISHED;

        // Interface
        interactionListener.onRecordingStateChange(recordingState);

        Realm realm = Realm.getDefaultInstance();

        DataEntry dataEntry = new DataEntry();
        dataEntry.generateId(realm);

        Attachment attachment = new Attachment();
        attachment.setFileType(FileUtils.Type.AUDIO)
                .setId(dataEntry.getId())
                .setFileName(file.getName());


        // TODO: 2/7/17 Improve code readability
        Uri uri = Uri.parse(file.getAbsolutePath());
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getContext(), uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);

        dataEntry.setLength(millSecond);

        dataEntry.setAudioFile(attachment);
        realm.executeTransaction(r -> {
            r.copyToRealmOrUpdate(dataEntry);
            r.copyToRealmOrUpdate(attachment);
        });


        this.recordingState = AudioRecorder.STATE.PENDING;
        buttonRecord.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
        buttonRecord.setImageResource(R.drawable.ic_mic);
        progressView.reset();
        audioRecorder.reset();

        new Handler(Looper.getMainLooper()).post(() -> {
            buttonRecord.setEnabled(true);     // Enable button click
        });
    }

    @Override
    public void onProgress(float progress, String text) {
        progressView.setProgress(progress, text);
    }

    @Override
    public void onSamples(short[] samples, int length) {
//        waveformView.setSamples(samples, length);
    }

    @Override
    public void onStop() {
        if (audioRecorder.getRecordingState() == AudioRecorder.STATE.RECORDING) {
            audioRecorder.stop();
        }
        super.onStop();
    }

    public interface OnFragmentInteractionListener {
        void onRecordingStateChange(AudioRecorder.STATE state);
    }
}