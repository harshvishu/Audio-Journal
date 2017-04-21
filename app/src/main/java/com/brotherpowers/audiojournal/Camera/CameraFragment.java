package com.brotherpowers.audiojournal.Camera;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.brotherpowers.audiojournal.Model.Attachment;
import com.brotherpowers.audiojournal.Model.DataEntry;
import com.brotherpowers.audiojournal.R;
import com.brotherpowers.audiojournal.Records.PhotosAdapter;
import com.brotherpowers.audiojournal.Utils.Constants;
import com.brotherpowers.audiojournal.Utils.DBHelper;
import com.brotherpowers.audiojournal.Utils.FileUtils;
import com.brotherpowers.audiojournal.View.ContextRecyclerView;
import com.brotherpowers.audiojournal.View.PermissionRequestFragment;
import com.brotherpowers.cameraview.CameraView;
import com.wefika.horizontalpicker.HorizontalPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {
    private static final String TAG = "CameraFragment";

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance(long entry_id) {
        Bundle args = new Bundle();
        args.putLong(Constants.KEYS.entry_id, entry_id);
        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

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

    private Handler mBackgroundHandler;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    // Interaction with the parent activity
    private CameraView.Callback cameraCallback
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

            System.out.println("..... picture taken .....");
            /**
             * Picture already taken
             *
             * On Google Pixel onPictureTaken is called Twice
             * Therefore using an atomic reference to check whether this is already been called
             * */
            if (pictureTaken.getAndSet(true)) {
                return;
            }

            getBackgroundHandler().post(() -> {

                try {
                    File file = FileUtils.sharedInstance.newImageFile(getContext());
                    OutputStream os = new FileOutputStream(file);

                    os.write(data);
                    os.close();
                    // Save file name in the records


                    final Realm realm = Realm.getDefaultInstance();

                    Attachment attachment = new Attachment();
                    attachment.generateId(realm)
                            .setFileName(file.getName())
                            .setFileType(FileUtils.Type.IMAGE);


                    realm.executeTransactionAsync(r -> {
                        final DataEntry entry = DBHelper.findEntryForId(entry_id, r).findFirst();
                        assert entry != null; // TODO: 2/22/17 remove

                        entry.getAttachments().add(attachment);
                    });
                    os.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    pictureTaken.set(false);
                }
            });

            disposable.add(Observable.just(data)
                    .map(bytes -> BitmapFactory.decodeByteArray(bytes, 0, data.length))
                    .subscribeOn(AndroidSchedulers.from(getBackgroundHandler().getLooper()))
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(bitmap -> {
                        _CaptureImageView.setTranslationX(0.0f);
                        _CaptureImageView.setImageBitmap(bitmap);
                        _CaptureImageView.setVisibility(View.VISIBLE);
                        return _CaptureImageView;
                    })
                    .map(imageView -> {
                        imageView.animate()
                                .setDuration(1000)
                                .translationXBy(1000)
//                                .translationYBy(100)
                                .setInterpolator(new OvershootInterpolator())
                                .start();
                        return imageView;
                    })
                    .delay(1200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<ImageView>() {
                        @Override
                        public void onNext(ImageView imageView) {
                            imageView.setVisibility(View.GONE);
                            imageView.setImageBitmap(null);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    }));

        }

        @Override
        public void supportedCameraModes(CameraView cameraView, SparseArray<String> modes) {
            uiHandler.post(() -> _colorModePicker.setValues(toArray(modes)));
        }

        @Override
        public void onCameraNotAvailable(CameraView cameraView) {
            Toast.makeText(getContext(), "There are no cameras available.", Toast.LENGTH_SHORT).show();
        }
    };


    @BindView(R.id.cameraView)
    CameraView _cameraView;

    @BindView(R.id.take_picture)
    ImageButton _imageButton;

    @BindView(R.id.color_mode_picker)
    HorizontalPicker _colorModePicker;

    @BindView(R.id.image_view)
    ImageView _CaptureImageView;

    @BindView(R.id.recycler_view_camera_images)
    ContextRecyclerView _RecyclerViewImages;

    private CompositeDisposable disposable = new CompositeDisposable();
    private int mCurrentFlash;
    private long entry_id;

    private PhotosAdapter photosAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        setRetainInstance(true);

        entry_id = getArguments().getLong(Constants.KEYS.entry_id, -1);

        Realm realm = Realm.getDefaultInstance();
        DataEntry entry = DBHelper.findEntryForId(entry_id, realm).findFirst();        // Sync
        if (entry != null) {
            photosAdapter = new PhotosAdapter(getContext(), DBHelper.images(entry).findAllAsync());
        }
        realm.close();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, view);

        /// Add callback to _cameraView
        _cameraView.addCallback(cameraCallback);

        /// Layout manger HORIZONTAL
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        _RecyclerViewImages.setLayoutManager(llm);
        /// Bind the photosAdapter with recycler_view_camera_images
        _RecyclerViewImages.setAdapter(photosAdapter);

        return view;
    }

    @OnTouch(R.id.take_picture)
    boolean handleTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final Animatable animatable;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final AnimatedVectorDrawable animatedDrawable = (AnimatedVectorDrawable) getContext().getDrawable(R.drawable.camera_s2l_anim);
                    animatable = animatedDrawable;
                    _imageButton.setImageDrawable(animatedDrawable);
                } else {
                    final AnimatedVectorDrawableCompat drawableCompat = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.camera_s2l_anim);
                    animatable = drawableCompat;
                    _imageButton.setImageDrawable(drawableCompat);
                }
                if (animatable != null) {
                    animatable.start();
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                final Animatable animatable;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final AnimatedVectorDrawable animatedDrawable = (AnimatedVectorDrawable) getContext().getDrawable(R.drawable.camera_l2s_anim);
                    animatable = animatedDrawable;
                    _imageButton.setImageDrawable(animatedDrawable);
                } else {
                    final AnimatedVectorDrawableCompat drawableCompat = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.camera_l2s_anim);
                    animatable = drawableCompat;
                    _imageButton.setImageDrawable(drawableCompat);
                }
                if (animatable != null) {
                    animatable.start();
                }
            }
            break;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();


        // Change camera color mode
        _colorModePicker.setOnItemSelectedListener(index -> _cameraView.setColorEffect(index));

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

            _cameraView.start();
            _cameraView.setAutoFocus(true);
            _cameraView.setFlash(CameraView.FLASH_AUTO);

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
        if (_cameraView.isCameraOpened()) {
            _cameraView.stop();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        // Remove callback
        _cameraView.removeCallback(cameraCallback);

        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
        // Dispose all
        disposable.dispose();
        super.onDestroy();
    }

    @OnClick(R.id.take_picture)
    void takePicture(ImageButton button) {
        System.out.println("take picture");
        _cameraView.takePicture();
    }

    @OnClick(R.id.action_flash)
    void changeFlash(ImageButton button) {
        if (_cameraView != null) {
            mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
            button.setImageResource(FLASH_ICONS[mCurrentFlash]);
            _cameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
        }
    }

    @SuppressWarnings("WrongConstant")
    @OnClick(R.id.action_switch_camera)
    void switchCamera() {
        if (_cameraView.isCameraOpened()) {
            int facing = 1 - _cameraView.getFacing();

            _cameraView.setFacing(facing);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }


    /**
     * Load image from attachment into imageView
     */
    /*private void loadImage(Attachment attachment) {
        final int thumbnailSize = getResources().getDimensionPixelSize(R.dimen.camera_control_size);
        Glide.with(getContext())
                .load(attachment.file(getContext()))
                .thumbnail(0.25f)
                .crossFade()
                .centerCrop()
                .bitmapTransform(new CircleTransform(getContext()))
                .override(thumbnailSize, thumbnailSize)
                .into(_imageView);
    }*/
    /*@OnClick(R.id.imageView)
    void onImageClick() {
        final Realm realm = Realm.getDefaultInstance();
        final DataEntry entry = DBHelper.findEntryForId(entry_id, realm).findFirst();        // Sync

        if (DBHelper.images(entry).count() > 0 && mListener != null) {
            mListener.openGalleryForDataEntry(entry_id);
        }
    }*/
    private static String[] toArray(SparseArray<String> sparseArray) {
        if (sparseArray == null) {
            return new String[]{};
        }
        String s[] = new String[sparseArray.size()];

        for (int i = 0; i < sparseArray.size(); i++)
            s[i] = sparseArray.valueAt(i);
        return s;
    }
}
