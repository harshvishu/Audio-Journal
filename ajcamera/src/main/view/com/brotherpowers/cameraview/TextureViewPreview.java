package com.brotherpowers.cameraview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by harsh_v on 3/7/17.
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class TextureViewPreview extends PreviewImpl {

    private final TextureView _textureView;
    private int mDisplayOrientation;

    TextureViewPreview(Context context, ViewGroup parent) {
        final View view = LayoutInflater.from(context).inflate(R.layout.texture_view, parent);
        _textureView = (TextureView) view.findViewById(R.id.texture_view);
        _textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setSize(width, height);
                configureTransform();
                dispatchSurfaceChanged();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                setSize(width, height);
                configureTransform();
                dispatchSurfaceChanged();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                setSize(0, 0);
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    /**
     * Configures the transform matrix for TextureView based on {@link #mDisplayOrientation} and
     * the surface size.
     */
    private void configureTransform() {
        Matrix matrix = new Matrix();
        if (mDisplayOrientation % 180 == 90) {
            final int width = getWidth();
            final int height = getHeight();
            // Rotate the camera preview when the screen is landscape.
            matrix.setPolyToPoly(
                    new float[]{
                            0.f, 0.f, // top left
                            width, 0.f, // top right
                            0.f, height, // bottom left
                            width, height, // bottom right
                    }, 0,
                    mDisplayOrientation == 90 ?
                            // Clockwise
                            new float[]{
                                    0.f, height, // top left
                                    0.f, 0.f, // top right
                                    width, height, // bottom left
                                    width, 0.f, // bottom right
                            } : // mDisplayOrientation == 270
                            // Counter-clockwise
                            new float[]{
                                    width, 0.f, // top left
                                    width, height, // top right
                                    0.f, 0.f, // bottom left
                                    0.f, height, // bottom right
                            }, 0,
                    4);
        }
        _textureView.setTransform(matrix);
    }

    @Override
    Surface getSurface() {
        return new Surface(_textureView.getSurfaceTexture());
    }

    @Override
    View getView() {
        return _textureView;
    }

    @Override
    Class getOutputClass() {
        return SurfaceTexture.class;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    @Override
    void setBufferSize(int width, int height) {
        _textureView.getSurfaceTexture().setDefaultBufferSize(width, height);
    }

    @Override
    Object getSurfaceTexture() {
        return _textureView.getSurfaceTexture();
    }

    @Override
    void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        configureTransform();
    }

    @Override
    boolean isReady() {
        return _textureView.getSurfaceTexture() != null;
    }

    @Override
    public void destroy() {

    }
}
