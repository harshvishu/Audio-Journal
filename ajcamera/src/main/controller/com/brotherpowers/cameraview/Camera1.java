package com.brotherpowers.cameraview;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by harsh_v on 3/1/17.
 */

public class Camera1 extends CameraViewImpl {

    private static final int INVALID_CAMERA_ID = -1;

    private static final SparseArrayCompat<String> FLASH_MODES = new SparseArrayCompat<>();

    static {
        FLASH_MODES.put(Constants.FLASH_OFF, Camera.Parameters.FLASH_MODE_OFF);
        FLASH_MODES.put(Constants.FLASH_ON, Camera.Parameters.FLASH_MODE_ON);
        FLASH_MODES.put(Constants.FLASH_TORCH, Camera.Parameters.FLASH_MODE_TORCH);
        FLASH_MODES.put(Constants.FLASH_AUTO, Camera.Parameters.FLASH_MODE_AUTO);
        FLASH_MODES.put(Constants.FLASH_RED_EYE, Camera.Parameters.FLASH_MODE_RED_EYE);
    }

    private int mCameraId;

    private Camera mCamera;

    private Camera.Parameters mCameraParameters;

    private final Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();

    private AspectRatio mAspectRatio;

    private boolean mShowingPreview;

    private boolean mAutoFocus;

    private int mFacing;

    private int mFlash;

    private final SizeMap mPreviewSizes = new SizeMap();

    private final SizeMap mPictureSizes = new SizeMap();

    private int mDisplayOrientation;

    private int mSensorOrientation;

    private final AtomicBoolean isPictureCaptureInProgress = new AtomicBoolean(false);

    private String mColorEffect = Camera.Parameters.EFFECT_NONE;

    private boolean hdr = false;


    Camera1(Callback callback, PreviewImpl preview) {
        super(callback, preview);
        preview.setCallback(new PreviewImpl.Callback() {
            @Override
            public void onSurfaceChanged() {
                if (mCamera != null) {
                    setUpPreview();
                    adjustCameraParameters();
                    mCallback.supportedCameraModes(getSupportedColorEffects());
                }
            }
        });
    }

    @Override
    boolean start() {
        chooseCamera();
        if (!openCamera()) {
            return false;
        }

        if (mPreview.isReady()) {
            setUpPreview();
        }
        mShowingPreview = true;
        mCamera.startPreview();
        return true;
    }


    @Override
    void stop() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        mShowingPreview = false;
        releaseCamera();
    }

    private void setUpPreview() {
        try {
            if (mPreview.getOutputClass() == SurfaceHolder.class) {
                final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
                if (needsToStopPreview) {
                    mCamera.stopPreview();
                }
                mCamera.setPreviewDisplay(mPreview.getSurfaceHolder());
                if (needsToStopPreview) {
                    mCamera.startPreview();
                }
            } else {
                mCamera.setPreviewTexture((SurfaceTexture) mPreview.getSurfaceTexture());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    boolean isCameraOpened() {
        return mCamera != null;
    }

    @Override
    void setFacing(int facing) {
        if (mFacing == facing) {
            return;
        }
        mFacing = facing;
        if (isCameraOpened()) {
            stop();
            start();
        }
    }

    @Override
    int getFacing() {
        return mFacing;
    }


    @Override
    boolean setAspectRatio(AspectRatio ratio) {
        if (mAspectRatio == null || !isCameraOpened()) {
            // Handle this later when camera is opened
            mAspectRatio = ratio;
            return true;
        } else if (!mAspectRatio.equals(ratio)) {
            final Set<Size> sizes = mPreviewSizes.sizes(ratio);
            if (sizes == null) {
                throw new UnsupportedOperationException(ratio + " is not supported");
            } else {
                mAspectRatio = ratio;
                adjustCameraParameters();
                return true;
            }
        }
        return false;
    }

    @Override
    AspectRatio getAspectRatio() {
        return mAspectRatio;
    }

    @Override
    void setAutoFocus(boolean autoFocus) {
        if (mAutoFocus == autoFocus) {
            return;
        }
        if (setAutoFocusInternal(autoFocus)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    boolean getAutoFocus() {
        if (!isCameraOpened()) {
            return mAutoFocus;
        }
        String focusMode = mCameraParameters.getFocusMode();
        return focusMode != null && focusMode.contains("continuous");
    }

    @Override
    void setFlash(int flash) {
        if (flash == mFlash) {
            return;
        }
        if (setFlashInternal(flash)) {
            mCamera.setParameters(mCameraParameters);
        }
    }

    @Override
    int getFlash() {
        return mFlash;
    }

    @Override
    void takePicture() {
        if (!isCameraOpened()) {
            throw new IllegalStateException(
                    "Camera is not ready. Call start() before takePicture().");
        }
        if (getAutoFocus()) {
            mCamera.cancelAutoFocus();
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    takePictureInternal();
                }
            });
        } else {
            takePictureInternal();
        }
    }

    private void takePictureInternal() {
        if (!isPictureCaptureInProgress.getAndSet(true)) {
            try {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
                        isPictureCaptureInProgress.set(false);
                        mCallback.onPictureTaken(data);
                        if (!isCameraOpened()) return;
                        camera.cancelAutoFocus();
                        mCamera.startPreview();
                    }
                });
            } catch (Exception e) {
                Log.e("CAMERA1", "unable to take picture", e);
            }
        }
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
        if (mDisplayOrientation == displayOrientation) {
            return;
        }
        mDisplayOrientation = displayOrientation;
        mCamera.setDisplayOrientation(calcCameraPreviewRotation(mDisplayOrientation));
    }

    @Override
    void setSensorOrientation(int sensorOrientation) {
        if (mSensorOrientation == sensorOrientation) {
            return;
        }
        mSensorOrientation = sensorOrientation;
        if (isCameraOpened()) {

            int cameraRotation = calcCameraImageRotation(mSensorOrientation);
            mCameraParameters.setRotation(cameraRotation);
            mCamera.setParameters(mCameraParameters);

            final boolean needsToStopPreview = mShowingPreview && Build.VERSION.SDK_INT < 14;
            if (needsToStopPreview) {
                mCamera.stopPreview();
            }
            if (needsToStopPreview) {
                mCamera.startPreview();
            }
        }
    }

    @Override
    void setColorEffect(int position) {
        if (isCameraOpened()) {
            mColorEffect = getSupportedColorEffects().valueAt(position);
            mCameraParameters.setColorEffect(mColorEffect);
        }
    }

    @Override
    protected SparseArray<String> getSupportedColorEffects() {
        final SparseArray<String> effects = new SparseArray<>();
        if (isCameraOpened()) {
            final List<String> supportedEffectsList = mCamera.getParameters().getSupportedColorEffects();

            if (!supportedEffectsList.isEmpty()) {
                for (int i = 0; i < supportedEffectsList.size(); i++) {
                    effects.put(i, supportedEffectsList.get(i));
                }
            }
        }
        return effects;
    }
    /************************************************
     * HELPER METHODS
     *****************/

    /**
     * This rewrites {@link #mCameraId} and {@link #mCameraInfo}.
     */
    private void chooseCamera() {
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, mCameraInfo);
            if (mCameraInfo.facing == mFacing) {
                mCameraId = i;
                return;
            }
        }
        mCameraId = INVALID_CAMERA_ID;
    }

    private boolean openCamera() {
        if (mCamera != null) {
            releaseCamera();
        }

        mCamera = null;
        if (mCameraId == INVALID_CAMERA_ID) {
            mCallback.onCameraNotAvailable();
            return false;
        }

        mCamera = Camera.open(mCameraId);
        mCameraParameters = mCamera.getParameters();
        // Supported preview sizes
        mPreviewSizes.clear();
        for (Camera.Size size : mCameraParameters.getSupportedPreviewSizes()) {
            mPreviewSizes.add(new Size(size.width, size.height));
        }
        // Supported picture sizes;
        mPictureSizes.clear();
        for (Camera.Size size : mCameraParameters.getSupportedPictureSizes()) {
            mPictureSizes.add(new Size(size.width, size.height));
        }
        // AspectRatio
        if (mAspectRatio == null) {
            mAspectRatio = Constants.DEFAULT_ASPECT_RATIO;
        }
        // Color Effect
        mCameraParameters.setColorEffect(mColorEffect);

        // HDR MODE
        // TODO: 3/17/17 PENDING
        adjustCameraParameters();
        mCamera.setDisplayOrientation(calcCameraPreviewRotation(mDisplayOrientation));
        mCallback.onCameraOpened();
        return true;
    }

    private AspectRatio chooseAspectRatio() {
        AspectRatio r = null;
        for (AspectRatio ratio : mPreviewSizes.ratios()) {
            r = ratio;
            if (ratio.equals(Constants.DEFAULT_ASPECT_RATIO)) {
                return ratio;
            }
        }
        return r;
    }

    private void adjustCameraParameters() {
        SortedSet<Size> sizes = mPreviewSizes.sizes(mAspectRatio);
        if (sizes == null) { // Not supported
            mAspectRatio = chooseAspectRatio();
            sizes = mPreviewSizes.sizes(mAspectRatio);
        }
        Size size = chooseOptimalSize(sizes);
        final Camera.Size currentSize = mCameraParameters.getPictureSize();
        if (currentSize.width != size.getWidth() || currentSize.height != size.getHeight()) {
            // Largest picture size in this ratio
            final Size pictureSize = mPictureSizes.sizes(mAspectRatio).last();
            if (mShowingPreview) {
                mCamera.stopPreview();
            }
            mCameraParameters.setPreviewSize(size.getWidth(), size.getHeight());
            mCameraParameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());

            // Set the camera image rotation
            mCameraParameters.setRotation(calcCameraImageRotation(mSensorOrientation));

            setAutoFocusInternal(mAutoFocus);
            setFlashInternal(mFlash);
            mCamera.setParameters(mCameraParameters);
            if (mShowingPreview) {
                mCamera.startPreview();
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        if (!mPreview.isReady()) { // Not yet laid out
            return sizes.first(); // Return the smallest size
        }
        int desiredWidth;
        int desiredHeight;
        final int surfaceWidth = mPreview.getWidth();
        final int surfaceHeight = mPreview.getHeight();
        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = null;
        for (Size size : sizes) { // Iterate from small to large
            if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                return size;

            }
            result = size;
        }
        return result;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            mCallback.onCameraClosed();
            mPreview.destroy();
        }
    }

    /**
     * Set the preview display rotation based on Device Orientation
     * <p>
     * NOTE: We are using only portrait in our app
     */
    private int calcCameraPreviewRotation(int rotation) {
        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 - (mCameraInfo.orientation + rotation) % 360) % 360;
        } else {  // back-facing
            return (mCameraInfo.orientation - rotation + 360) % 360;
        }
    }

    /**
     * sets orientation of images based on Motion Sensor
     * <p>
     * NOTE: App must be in portrait position unless it won't give correct result
     */
    private int calcCameraImageRotation(int rotation) {
        // Ignore the passed value
        rotation = mSensorOrientation;

        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            //noinspection UnnecessaryLocalVariable
            final int calculation = (360 - (mCameraInfo.orientation + rotation + (rotation % 180 == 0 ? 180 : 0)) % 360) % 360;
            return calculation;
        } else {  // back-facing
            return (mCameraInfo.orientation - rotation + 360) % 360;
        }
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    @SuppressLint("InlinedApi")
    private boolean setAutoFocusInternal(boolean autoFocus) {
        mAutoFocus = autoFocus;
        if (isCameraOpened()) {
            final List<String> modes = mCameraParameters.getSupportedFocusModes();
            if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
            } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            } else {
                mCameraParameters.setFocusMode(modes.get(0));
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return {@code true} if {@link #mCameraParameters} was modified.
     */
    private boolean setFlashInternal(int flash) {
        if (isCameraOpened()) {
            List<String> modes = mCameraParameters.getSupportedFlashModes();
            String mode = FLASH_MODES.get(flash);
            if (modes != null && modes.contains(mode)) {
                mCameraParameters.setFlashMode(mode);
                mFlash = flash;
                return true;
            }
            String currentMode = FLASH_MODES.get(mFlash);
            if (modes == null || !modes.contains(currentMode)) {
                mCameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlash = Constants.FLASH_OFF;
                return true;
            }
            return false;
        } else {
            mFlash = flash;
            return false;
        }
    }
}
