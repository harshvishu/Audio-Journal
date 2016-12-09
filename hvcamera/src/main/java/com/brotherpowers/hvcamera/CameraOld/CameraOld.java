package com.brotherpowers.hvcamera.CameraOld;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.TextureView;
import android.view.View;

import com.brotherpowers.hvcamera.CameraFragmentInteractionInterface;
import com.brotherpowers.hvcamera.HVBaseFragment;
import com.brotherpowers.hvcamera.R;

import java.io.IOException;

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


    private static final int REQ_CAM_PERMISSION = 0x233;


    public CameraOld() {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment CameraOld.
     */
    public static CameraOld newInstance(boolean frontCameraSupported, boolean isFrontCamera) {
        CameraOld fragment = new CameraOld();
        Bundle args = new Bundle();
        args.putBoolean(ARG_FRONT_CAMERA_SUPPORTED, frontCameraSupported);
        args.putBoolean(ARG_IS_FRONT_CAMERA, isFrontCamera);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    private Camera camera;

    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        try {
            camera = new CameraConfig.Builder()
                    .setAutoFocus(true)
                    .useBackCamera()
                    .setFlashMode(CameraConfig.FlashMode.AUTO)
                    .build()
                    .open(getContext());

            if (camera!=null){
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                    try {
                        camera.setPreviewTexture(surfaceTexture);
                        camera.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        // Button Flash
        if (id == R.id.flash) {
//            actionFlash();
        }
        // Button Picture
        else if (id == R.id.picture) {
//            actionTakePicture();
        }
        // Button Camera Switch
        else if (id == R.id.switch_camera) {
//            actionSwitchCamera();
        }

        new CameraConfig.Builder();
    }


    /**
     * @param flashMode {@link CameraConfig.FlashMode }
     *                  <p>
     *                  set the icon for flash mode
     */
    private void setFlashButtonResource(CameraConfig.FlashMode flashMode) {
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

                        // TODO: 12/9/16 pending

//                        int angle = CameraUtil.getPortraitCameraDisplayOrientation(getContext(), cameraId, isFrontCamera);
//                        matrix.postRotate(angle);

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


}
