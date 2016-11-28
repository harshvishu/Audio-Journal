package com.brotherpowers.hvcamerafragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ragnarok.rxcamera.RxCamera;
import com.ragnarok.rxcamera.config.CameraUtil;
import com.ragnarok.rxcamera.config.RxCameraConfig;
import com.ragnarok.rxcamera.request.Func;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment implements AutoFitTextureView.TouchHandler {
    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "CameraFragment";
    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQ_CAM_PERMISSION = 0x1;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private RxCamera camera;
    private CameraInterface cameraInterface = new CameraInterface() {
        @Override
        public void onCapturePicture(Bitmap bitmap) {

        }
    };


    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment newInstance() {

        Bundle args = new Bundle();

        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }


    AutoFitTextureView mTextureView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        System.out.println(">>>>>> VIEW CREATED ");

//        openCamera(MAX_PREVIEW_WIDTH, MAX_PREVIEW_WIDTH);

        mTextureView.setTouchHandler(this);

    }


    @Override
    public void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            System.out.println(">>>> REQ PERMISSION");
            requestCameraPermission();
            return;
        }

        System.out.println(">>>>>> PERMISSION GRANTED ");

        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);

        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        maxPreviewWidth = MAX_PREVIEW_WIDTH;
//        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
//        }
//
//        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
//        }
        maxPreviewHeight = MAX_PREVIEW_HEIGHT;


        RxCameraConfig config = new RxCameraConfig.Builder()
                .useBackCamera()
                .setAutoFocus(true)
                .setPreferPreviewFrameRate(15, 30)
                .setPreferPreviewSize(new Point(maxPreviewWidth, maxPreviewHeight), false)
                .setHandleSurfaceEvent(true)
                .build();

        RxCamera.open(getContext(), config).flatMap(new Func1<RxCamera, Observable<RxCamera>>() {
            @Override
            public Observable<RxCamera> call(RxCamera rxCamera) {
                return rxCamera.bindTexture(mTextureView);
                // or bind a SurfaceView:
                // rxCamera.bindSurface(SurfaceView)
            }
        }).flatMap(new Func1<RxCamera, Observable<RxCamera>>() {
            @Override
            public Observable<RxCamera> call(RxCamera rxCamera) {
                return rxCamera.startPreview();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<RxCamera>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(final RxCamera rxCamera) {
                camera = rxCamera;
                Toast.makeText(getActivity(), "Now you can tap to focus", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Permission Denied")
                    .setMessage("App needs permission to access the camera and storage feature")
                    .setPositiveButton("Ok", (dialogInterface, i) -> {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CAM_PERMISSION);

                    })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {

                    })
                    .show();

        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CAM_PERMISSION);
        }

    }


    /**
     * Capture Picture
     */
    private void requestTakePicture() {
        if (!checkCamera()) {
            return;
        }

        camera.request().takePictureRequest(true, new Func() {
            @Override
            public void call() {
                showLog("Captured!");
            }
        }, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, ImageFormat.JPEG, true).subscribe(rxCameraData -> {
//            String path = Environment.getExternalStorageDirectory() + "/test.jpg";
//            File file = new File(path);

            Bitmap bitmap = BitmapFactory.decodeByteArray(rxCameraData.cameraData, 0, rxCameraData.cameraData.length);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    rxCameraData.rotateMatrix, false);

            cameraInterface.onCapturePicture(bitmap);

            /*try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
//            showLog("Save file on " + path);
        });
    }


    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * @param cameraInterface {@link CameraInterface}
     */
    public void setCameraInterface(CameraInterface cameraInterface) {
        this.cameraInterface = cameraInterface;
    }

    /**
     * Open FLASH
     */
    private void actionOpenFlash() {
        if (!checkCamera()) {
            return;
        }
        camera.action().flashAction(true).subscribe(new Subscriber<RxCamera>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                showLog("open flash error: " + e.getMessage());
            }

            @Override
            public void onNext(RxCamera rxCamera) {
                showLog("open flash");
            }
        });
    }

    /**
     * Close FLASH
     */
    private void actionCloseFlash() {
        if (!checkCamera()) {
            return;
        }
        camera.action().flashAction(false).subscribe(new Subscriber<RxCamera>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                showLog("close flash error: " + e.getMessage());
            }

            @Override
            public void onNext(RxCamera rxCamera) {
                showLog("close flash");
            }
        });
    }

    /**
     * Helper function to show log
     *
     * @param s data
     */
    private void showLog(String s) {
        Log.v(TAG, s);
    }

    /**
     * Turn on Face Detection
     */
    private void faceDetection() {
        camera.request().faceDetectionRequest().subscribe(rxCameraData -> {
            Log.v(TAG, "on face detection: " + Arrays.toString(rxCameraData.faceList));
        });
    }

    /**
     * switch camera
     */
    private void switchCamera() {
        if (!checkCamera()) {
            return;
        }
        camera.switchCamera().subscribe(aBoolean -> {
            Log.v(TAG, "switch camera result: " + aBoolean);
        });
    }

    /**
     * @return true is we can use camera
     */
    private boolean checkCamera() {
        return !(camera == null || !camera.isOpenCamera());
    }

    @Override
    public boolean onTouch(MotionEvent event) {
        if (!checkCamera()) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            final float x = event.getX();
            final float y = event.getY();
            final Rect rect = CameraUtil.transferCameraAreaFromOuterSize(new Point((int) x, (int) y),
                    new Point(mTextureView.getWidth(), mTextureView.getHeight()), 100);
            List<Camera.Area> areaList = Collections.singletonList(new Camera.Area(rect, 1000));
            Observable.zip(camera.action().areaFocusAction(areaList),
                    camera.action().areaMeterAction(areaList),
                    new Func2<RxCamera, RxCamera, Object>() {
                        @Override
                        public Object call(RxCamera rxCamera, RxCamera rxCamera2) {
                            return rxCamera;
                        }
                    }).subscribe(new Subscriber<Object>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "area focus and metering failed: " + e.getMessage());
                }

                @Override
                public void onNext(Object o) {
                    Log.e(TAG, String.format("area focus and metering success, x: %s, y: %s, area: %s", x, y, rect.toShortString()));
                }
            });
        }
        return false;
    }

    interface CameraInterface {
        void onCapturePicture(Bitmap bitmap);
    }

}
