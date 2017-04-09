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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.brotherpowers.audiojournal.Main.FragmentSections;
import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.AudioJournalPreferences;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.Utils.Extensions;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.PermissionRequestFragment;
import com.brotherpowers.hvprogressview.ProgressView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;

/**
 * A simple {@link Fragment} subclass.
 */
public class AudioRecordingFragment extends Fragment implements AudioRecorder.Listener {

    // Static instance for audio recorder
    private static AudioRecorder audioRecorder;

    @BindView(R.id.progress_view)
    ProgressView progressView;

    @BindView(R.id.action_capture)
    FloatingActionButton buttonRecord;

    @BindView(R.id.label_max_recording_duration)
    TextView labelMaxDuration;

    @BindView(R.id.label_total_recording_duration)
    TextView labelTotalRecordingDuration;

    @BindView(R.id.label_total_records)
    TextView labelTotalRecords;

    private AudioRecorder.STATE recordingState;
    private OnFragmentInteractionListener interactionListener;
    private AudioJournalPreferences preferences;
    private Realm realm;
    public AudioRecordingFragment() {
        // Required empty public constructor
    }

    public static AudioRecordingFragment newInstance() {
        Bundle args = new Bundle();
        AudioRecordingFragment fragment = new AudioRecordingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Initialize the preferences
        preferences = new AudioJournalPreferences(getContext());

        // Initialize Realm
        realm = Realm.getDefaultInstance();

        audioRecorder = new AudioRecorder(getContext(), preferences.getMaxRecordingDuration());
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
        buttonRecord.setImageResource(recordingState == AudioRecorder.STATE.RECORDING ? R.drawable.ic_stop : R.drawable.ic_mic);

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
                        .show(getChildFragmentManager(), Constants.FRAGMENT_DIALOG);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_REC_PERMISSION);
            }

        });


        setLabelMaxDuration();

        setInfoLabels();

        return view;
    }

    private void setInfoLabels() {
        labelTotalRecordingDuration.setText(FragmentSections.recorder.title(realm));
        labelTotalRecords.setText(FragmentSections.records.title(realm));
    }

    // TODO: 2/22/17 replace with databinding
    private void setLabelMaxDuration() {
        int maxduration = preferences.getMaxRecordingDuration();
        labelMaxDuration.setText(Extensions.millisToMS(maxduration));
    }


    @Override
    public void onRecordingStart() {
        this.recordingState = AudioRecorder.STATE.RECORDING;

        // Interface
        interactionListener.onRecordingStateChange(recordingState);

        changeButtonDrawableWithAnim(R.drawable.ic_stop);

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
//            r.copyToRealmOrUpdate(attachment);
        });


        this.recordingState = AudioRecorder.STATE.PENDING;
        changeButtonDrawableWithAnim(R.drawable.ic_mic);
        progressView.reset();
        audioRecorder.reset();

        new Handler(Looper.getMainLooper()).post(() -> {
            buttonRecord.setEnabled(true);     // Enable button click
        });
    }

    private void changeButtonDrawableWithAnim(int drawable) {
        // FIXME: 2/18/17 Use proper animation
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.record_button_animations_set);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                buttonRecord.setImageResource(drawable);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        buttonRecord.startAnimation(animation);
    }

    @Override
    public void onProgress(float progress, String text) {
        progressView.set(progress, text);
    }

    @Override
    public void onSamples(short[] samples, int length) {
//        waveformView.setSamples(samples, length);
    }

    @Override
    public void onStart() {
        super.onStart();
        realm.addChangeListener(element -> {
            labelTotalRecordingDuration.setText(FragmentSections.recorder.title(realm));
            labelTotalRecords.setText(FragmentSections.records.title(realm));
        });
    }

    @Override
    public void onStop() {
        if (audioRecorder.getRecordingState() == AudioRecorder.STATE.RECORDING) {
            audioRecorder.stop();
        }
        realm.removeAllChangeListeners();
        super.onStop();
    }

    public interface OnFragmentInteractionListener {
        void onRecordingStateChange(AudioRecorder.STATE state);
    }
}
