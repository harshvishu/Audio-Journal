package com.brotherpowers.hvcamera.CameraOld;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;

/**
 * Created by harsh_v on 12/9/16.
 */

class CameraConfig {

    private static final int CAM_DEF_ID = -1;

    private int currentCameraId = CAM_DEF_ID;
    private int frontCameraId = CAM_DEF_ID;
    private int backCameraId = CAM_DEF_ID;

    private FlashMode flashMode = FlashMode.AUTO;
    private boolean frontCameraSupported;
    private boolean autoFocus = true;

    /**
     * @return true is using front camera false otherwise
     */
    boolean isUsingFrontCamera() {
        return currentCameraId == frontCameraId &&
                frontCameraId != CAM_DEF_ID;
    }

    /**
     * @return true is using back camera false otherwise
     */
    boolean isUsingBackCamera() {
        return currentCameraId == backCameraId &&
                backCameraId != CAM_DEF_ID;
    }

    /**
     * @return FlashMode default is AUTO
     */
    public FlashMode getFlashMode() {
        return flashMode;
    }

    enum FlashMode {
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

    static class Builder {
        private CameraConfig cameraConfig = new CameraConfig();

        CameraConfig build() {
            return cameraConfig;
        }

        Builder setFlashMode(FlashMode flashMode) {
            cameraConfig.flashMode = flashMode;
            return this;
        }

        Builder setAutoFocus(boolean value) {
            cameraConfig.autoFocus = value;
            return this;
        }

        Builder useFrontCamera() {
            if (cameraConfig.frontCameraId == CAM_DEF_ID) {
                // set front camera id
                cameraConfig.frontCameraId = CameraUtil.getFrontCameraId();
                // set current camera id
                cameraConfig.currentCameraId = cameraConfig.frontCameraId;
            } else {
                // set current camera id
                cameraConfig.currentCameraId = cameraConfig.frontCameraId;
            }
            return this;
        }

        Builder useBackCamera() {
            if (cameraConfig.backCameraId == CAM_DEF_ID) {
                // set back camera id
                cameraConfig.backCameraId = CameraUtil.getBackCameraId();
                // set current camera id
                cameraConfig.currentCameraId = cameraConfig.backCameraId;
            } else {
                // set current camera id
                cameraConfig.currentCameraId = cameraConfig.backCameraId;
            }
            return this;
        }


    }

    Camera open(Context context) throws Exception {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        Camera camera = Camera.open(currentCameraId);

        setup(camera, context);

        return camera;
    }

    private CameraConfig setup(Camera camera, Context context) {
        Camera.Parameters parameters = camera.getParameters();

        // 1. Set flash
        if (parameters.getSupportedFlashModes() != null) {
            switch (flashMode) {
                case AUTO:
                    if (parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    }
                    break;
                case ON:
                    if (parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    }
                    break;
                case OFF:
                    if (parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_OFF)) {
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    }
                    break;
            }
        }

        // 2. Set Auto Focus
        if (parameters.getSupportedFocusModes() != null) {
            if (autoFocus && parameters.getSupportedFlashModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
//            else if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
//            }
        }


        // Bind Parameters
        camera.setParameters(parameters);

        camera.setDisplayOrientation(
                CameraUtil.getPortraitCameraDisplayOrientation(context,
                        currentCameraId, isUsingFrontCamera())
        );


        return this;
    }
}
