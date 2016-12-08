package com.brotherpowers.hvcamera.CameraOld;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.brotherpowers.hvcamera.CameraFragmentInteractionInterface;
import com.brotherpowers.hvcamera.HVBaseFragment;
import com.brotherpowers.hvcamera.R;

import java.io.IOException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CameraOld#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraOld extends HVBaseFragment {
    private static final String ARG_FRONT_CAMERA_SUPPORTED = "front_camera_supported";
    private static final String ARG_IS_FRONT_CAMERA = "is_front_camera";
    private static final String ARG_FLASH_MODE = "flash_mode";
    private static final String ARG_CAMERA_ID = "camera_id";


    public enum FlashMode {
        AUTO(0, Camera.Parameters.FLASH_MODE_AUTO), ON(1, Camera.Parameters.FLASH_MODE_ON), OFF(2, Camera.Parameters.FLASH_MODE_OFF);

        final int value;
        final String param;

        FlashMode(int value, String param) {
            this.value = value;
            this.param = param;
        }

        // get Enum for integer value
        static FlashMode self(int value) {
            switch (value) {
                case 0:
                    return AUTO;
                case 1:
                    return ON;
                case 2:
                    return OFF;
                default:
                    return OFF;
            }
        }
    }

    public CameraOld() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment CameraOld.
     */
    public static CameraOld newInstance(boolean frontCameraSupported, boolean isFrontCamera, FlashMode flashMode) {
        CameraOld fragment = new CameraOld();
        Bundle args = new Bundle();
        args.putBoolean(ARG_FRONT_CAMERA_SUPPORTED, frontCameraSupported);
        args.putBoolean(ARG_IS_FRONT_CAMERA, isFrontCamera);
        args.putInt(ARG_FLASH_MODE, flashMode.value);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    private Camera camera;

    private int cameraId = -1;
    private boolean isFrontCamera = false;
    private boolean isFrontCameraSupported = false;
    private FlashMode flashMode;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null && getArguments() == null) {
            isFrontCameraSupported = CameraUtil.hasFrontCamera();
            isFrontCamera = false;
            cameraId = CameraUtil.getBackCameraId();
            flashMode = FlashMode.OFF;

            setUpCamera(cameraId, false, FlashMode.OFF);

        } else if (savedInstanceState == null) {

            // Set up front camera
            isFrontCameraSupported = CameraUtil.hasFrontCamera() && getArguments().getBoolean(ARG_FRONT_CAMERA_SUPPORTED);

            isFrontCamera = getArguments().getBoolean(ARG_IS_FRONT_CAMERA);

            if (isFrontCamera) {
                cameraId = CameraUtil.getFrontCameraId();
            } else {
                cameraId = CameraUtil.getBackCameraId();
            }

            flashMode = FlashMode.self(getArguments().getInt(ARG_FLASH_MODE));

        } else {

            isFrontCameraSupported = CameraUtil.hasFrontCamera() && savedInstanceState.getBoolean(ARG_FRONT_CAMERA_SUPPORTED);

            isFrontCamera = savedInstanceState.getBoolean(ARG_IS_FRONT_CAMERA);
            cameraId = savedInstanceState.getInt(ARG_CAMERA_ID);
            flashMode = FlashMode.self(savedInstanceState.getInt(ARG_FLASH_MODE));

        }

        setUpCamera(cameraId, isFrontCamera, flashMode);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                try {
                    camera.setPreviewTexture(surfaceTexture);
                    camera.startPreview();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {

                    }
                });
                return true;
            }
        });


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ARG_CAMERA_ID, cameraId);
        outState.putBoolean(ARG_FRONT_CAMERA_SUPPORTED, isFrontCameraSupported);
        outState.putBoolean(ARG_IS_FRONT_CAMERA, isFrontCamera);
        outState.putInt(ARG_FLASH_MODE, flashMode.ordinal());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Button Flash
        if (id == R.id.flash) {
            actionFlash();
        }
        // Button Picture
        else if (id == R.id.picture) {
            actionTakePicture();
        }
        // Button Camera Switch
        else if (id == R.id.switch_camera && isFrontCameraSupported) {

            actionSwitchCamera();
        }

    }

    private void actionTakePicture() {
        try {
            camera.takePicture(null, null, pictureCallback);
            buttonSwitchCamera.setEnabled(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionSwitchCamera() {
        camera.stopPreview();
        camera.release();
        camera = null;


        if (isFrontCamera) {
            cameraId = CameraUtil.getBackCameraId();
            isFrontCamera = false;
        } else {
            cameraId = CameraUtil.getFrontCameraId();
            isFrontCamera = true;
        }

        setUpCamera(cameraId, isFrontCamera, flashMode);

        try {
            camera.setPreviewTexture(textureView.getSurfaceTexture());
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    private void actionFlash() {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFlashModes() != null &&
                parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {

            switch (flashMode) {
                case AUTO: {
                    flashMode = FlashMode.ON;

                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    camera.setParameters(parameters);
                }
                break;
                case ON: {
                    flashMode = FlashMode.OFF;

                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);

                }
                break;
                case OFF: {
                    flashMode = FlashMode.AUTO;

                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    camera.setParameters(parameters);

                }
                break;
            }

            setFlashButtonResource(flashMode);

        } else {
            Toast.makeText(getContext(), "Flash not supported", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param cameraId
     * @param isFrontCamera
     * @param flashMode
     */
    private void setUpCamera(int cameraId, boolean isFrontCamera, FlashMode flashMode) {

        camera = Camera.open(cameraId);
        Camera.Parameters parameters = camera.getParameters();

        // Set Camera Focus Mode
        try {
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set Camera flash
        if (parameters.getSupportedFlashModes() != null &&
                parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
            parameters.setFlashMode(flashMode.param);
        }

        setFlashButtonResource(flashMode);

        // Set Size
        Camera.Size size = CameraUtil.findClosestNonSquarePreviewSize(camera, new Point(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT));
        if (size != null) {
            parameters.setPictureSize(size.width, size.height);

        }

        camera.setParameters(parameters);

        // Orientation FIX
        int angle = CameraUtil.getPortraitCameraDisplayOrientation(getContext(), cameraId, isFrontCamera);
        camera.setDisplayOrientation(angle);

        if (!isFrontCameraSupported) {
            buttonSwitchCamera.setEnabled(false);
            buttonSwitchCamera.setVisibility(View.INVISIBLE);
        } else {
            buttonSwitchCamera.setEnabled(true);
            buttonSwitchCamera.setVisibility(View.VISIBLE);
        }

    }

    /**
     * @param flashMode {@link CameraOld.FlashMode }
     *                  <p>
     *                  set the icon for flash mode
     */
    private void setFlashButtonResource(FlashMode flashMode) {
        switch (flashMode) {
            case AUTO:
                buttonFlash.setImageResource(R.drawable.ic_flash_auto);
                break;
            case ON:
                buttonFlash.setImageResource(R.drawable.ic_flash_on);
                break;
            case OFF:
                buttonFlash.setImageResource(R.drawable.ic_flash_off);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        if (camera != null) {
            camera.release();
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBackgroundThread();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            interactionInterface = (CameraFragmentInteractionInterface) getActivity();

        } catch (ClassCastException e) {
            throw new RuntimeException("Implement CameraFragmentInteractionInterface in activity");
        }


    }

    /**
     * Picture Callback
     * <p>
     * Called after {@link Camera#takePicture(Camera.ShutterCallback, Camera.PictureCallback, Camera.PictureCallback, Camera.PictureCallback)}
     */
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] bytes, Camera camera) {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    buttonSwitchCamera.setEnabled(true);
                }
            });

            buttonSwitchCamera.setEnabled(false);

            try {
                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {


                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Matrix matrix = new Matrix();

                        int angle = CameraUtil.getPortraitCameraDisplayOrientation(getContext(), cameraId, isFrontCamera);
                        matrix.postRotate(angle);

                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                        interactionInterface.onImageCaptured(bitmap);

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Catch any exception
            }

            camera.startPreview();

        }
    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBackgroundThread.quitSafely();
        }
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
