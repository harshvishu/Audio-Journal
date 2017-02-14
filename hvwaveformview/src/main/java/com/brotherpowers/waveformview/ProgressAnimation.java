package com.brotherpowers.waveformview;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by harsh_v on 7/22/16.
 *
 */
public class ProgressAnimation extends Animation {
    private WaveformView waveformView;
    private float oldMarkerPosition;
    private float newMarkerPosition;

    public ProgressAnimation(WaveformView waveformView) {
        this.waveformView = waveformView;
    }

    public ProgressAnimation preExecute(float mMarkerPosition) {
        this.oldMarkerPosition = waveformView.getMarkerPosition();
        this.newMarkerPosition= mMarkerPosition;
        return this;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float value = oldMarkerPosition + ((oldMarkerPosition - newMarkerPosition) * interpolatedTime);
//        waveformView.setMarkerPosition(value);
        waveformView.requestLayout();
    }
}
