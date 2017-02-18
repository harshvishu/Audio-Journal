package com.brotherpowers.audiojournal.Camera;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.PermissionRequestFragment;
import com.bumptech.glide.Glide;
import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance(long entry_id) {
        Bundle args = new Bundle();
        args.putLong("id", entry_id);
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.cameraview)
    CameraView mCameraView;

    @BindView(R.id.imageview)
    ImageView _imageView;

    private Handler mBackgroundHandler;
    private int mCurrentFlash;
    private ProgressDialog progressDialog;
    private long entry_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        entry_id = getArguments().getLong("id", -1);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            mCameraView.start();
            mCameraView.setAutoFocus(true);
            mCameraView.setFlash(CameraView.FLASH_AUTO);

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            PermissionRequestFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            Constants.REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getChildFragmentManager(), Constants.FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCameraView.removeCallback(mCallback);

        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @OnClick(R.id.take_picture)
    void takePicture() {
        if (mCameraView != null) {
            mCameraView.takePicture();
        }
    }

    @OnClick(R.id.action_flash)
    void changeFlash(ImageButton button){
        if (mCameraView != null) {
            mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
            button.setImageResource(FLASH_ICONS[mCurrentFlash]);
            mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
        }
    }


    private CameraView.Callback mCallback
            = new CameraView.Callback() {

        // Wil allow to take only one picture for this activity
        private AtomicBoolean pictureTaken = new AtomicBoolean(false);

        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data, final int sensorOrientation, final int displayOrientation) {

            /*
            * Picture already taken
            * */
           /* if (pictureTaken.getAndSet(true)) {
                return;
            }*/

            new Handler(Looper.getMainLooper())
                    .post(() -> progressDialog = ProgressDialog.show(cameraView.getContext(), null, "saving...", true, false));

            getBackgroundHandler().post(() -> {


                try {
                    File file = getFile(getContext());
                    OutputStream os = new FileOutputStream(file);

                    os.write(data);
                    os.close();
                    // Save file name in the records


                    final Realm realm = Realm.getDefaultInstance();
                    final DataEntry entry = realm.where(DataEntry.class).equalTo("id", entry_id).findFirst();
                    assert entry != null;

                    Attachment attachment = new Attachment();
                    attachment.generateId(realm)
                            .setFileName(file.getName())
                            .setFileType(FileUtils.Type.IMAGE);

                    // Persist to disk Async
                    realm.executeTransactionAsync(r -> r.copyToRealmOrUpdate(attachment));

                    // Load the image
                    Glide.with(getContext())
                            .load(attachment.file(getContext()))
                            .fitCenter()
                            .into(_imageView);



                    os.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    pictureTaken.set(false);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "Picture Taken", Toast.LENGTH_SHORT).show();

                    });
                }
            });
        }
    };

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    // GET the desired file
    private File getFile(Context context) {
        return FileUtils.sharedInstance.getFile(FileUtils.Type.IMAGE, String.valueOf(System.currentTimeMillis()), context);
    }


}
