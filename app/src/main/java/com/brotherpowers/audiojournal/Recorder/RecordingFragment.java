package com.brotherpowers.audiojournal.Recorder;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
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
import com.brotherpowers.audiojournal.Realm.DataEntry;
import com.brotherpowers.audiojournal.Realm.RFile;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.ConfirmationDialogFragment;
import com.brotherpowers.hvprogressview.ProgressView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

import static com.brotherpowers.audiojournal.Recorder.AudioRecorder.MAX_AUDIO_LENGTH;
import static com.brotherpowers.audiojournal.Utils.Constants.REQ_REC_PERMISSION;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecordingFragment extends Fragment implements AudioRecorder.Listener {
    private static final String FRAGMENT_DIALOG = "dialog";

    @BindView(R.id.progress_view)
    ProgressView progressView;

    @BindView(R.id.action_capture)
    FloatingActionButton buttonCapture;

    public RecordingFragment() {
        // Required empty public constructor
    }

    public static RecordingFragment newInstance() {

        Bundle args = new Bundle();

        RecordingFragment fragment = new RecordingFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private static AudioRecorder audioRecorder;
    private AudioRecorder.STATE recordingState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);


        audioRecorder = new AudioRecorder(getContext(), MAX_AUDIO_LENGTH);
        audioRecorder.setListener(this);
        recordingState = audioRecorder.getRecordingState();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recording, container, false);
        ButterKnife.bind(this, view);


        if (recordingState == AudioRecorder.STATE.RECORDING) {
            buttonCapture.setImageResource(R.drawable.ic_stop);
        } else {
            buttonCapture.setImageResource(R.drawable.ic_mic);
        }

        buttonCapture.setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {

                if (recordingState == AudioRecorder.STATE.PENDING) {
                    audioRecorder.start(getContext());
                } else if (recordingState == AudioRecorder.STATE.RECORDING) {
                    audioRecorder.stop();
                }

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.RECORD_AUDIO)) {
                ConfirmationDialogFragment
                        .newInstance(R.string.record_permission_confirmation,
                                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQ_REC_PERMISSION,
                                R.string.record_permission_not_granted)
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            } else {
                System.out.println(">>>>> REQUEST PEMISSION");
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_REC_PERMISSION);
            }

        });
        return view;
    }


    @Override
    public void onRecordingStart(AudioRecorder.STATE recordingState) {
        this.recordingState = recordingState;

        buttonCapture.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
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


        // TODO: 2/7/17 Improve code readability
        Uri uri = Uri.parse(file.getAbsolutePath());
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(getContext(), uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);
        dataEntry.setLength(millSecond);

        dataEntry.setAudioFile(rFile);
        realm.executeTransaction(r -> {
            r.copyToRealmOrUpdate(dataEntry);
            r.copyToRealmOrUpdate(rFile);
        });


        this.recordingState = AudioRecorder.STATE.PENDING;
        buttonCapture.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_open));
        buttonCapture.setImageResource(R.drawable.ic_mic);
        progressView.reset();
        audioRecorder.reset();
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
