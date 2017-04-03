package com.brotherpowers.waveformview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;
import java.util.Locale;

import static com.brotherpowers.waveformview.Utils.getExtremes;

/**
 * Created by harsh_v on 11/21/16.
 */

public class WaveformView extends View {
    private static final int HISTORY_SIZE = 2;
    public static final int MODE_RECORDING = 1;
    public static final int MODE_PLAYBACK = 2;


    public WaveformView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private Paint mTextPaint;
    private Paint mWaveFillPaint;
    private Paint mMarkerPaint;
    protected Paint mProgressPaint;

    private int mMode;
    private int mAudioLength;
    private float markerPosition;
    private int mSampleRate;
    private int mChannels;
    private short[] mSamples;
    private boolean showTextAxis = true;

    private Path waveformPath;
    @SuppressWarnings("FieldCanBeLocal")
    private Bitmap mCachedWaveformBitmap;
    private Bitmap playbackBitmap;
    private Canvas playbackCanvas;
    private Rect drawRect;
    private Picture mCachedWaveform;
    private LinkedList<float[]> mHistoricalData;


    private int width;
    private int height;
    private float xStep;
    private float centerY;

    private ObjectAnimator animator;

    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveformView, defStyle, 0);

        final int waveFillColor = array.getColor(R.styleable.WaveformView_waveFillColor, ContextCompat.getColor(context, R.color.wave));
        final int markerColor = array.getColor(R.styleable.WaveformView_markerColor, ContextCompat.getColor(context, R.color.marker));
        final int textColor = array.getColor(R.styleable.WaveformView_timeCodeColor, ContextCompat.getColor(context, R.color.text));

        mMode = array.getInt(R.styleable.WaveformView_mode, MODE_PLAYBACK);
        array.recycle();

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(Utils.getFontSize(context, android.R.attr.textAppearance));

        mWaveFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWaveFillPaint.setStyle(Paint.Style.FILL);
        mWaveFillPaint.setColor(waveFillColor);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.FILL);
        mProgressPaint.setColor(markerColor);
        mProgressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        mMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerPaint.setStyle(Paint.Style.FILL);
        mMarkerPaint.setColor(markerColor);

        waveformPath = new Path();
        mHistoricalData = new LinkedList<>();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = getMeasuredWidth();
        height = getMeasuredHeight();
        xStep = width / (mAudioLength * 1.0f);
        centerY = height / 2f;
        drawRect = new Rect(0, 0, width, height);

        if (mHistoricalData != null) {
            mHistoricalData.clear();
        }
        if (mMode == MODE_PLAYBACK) {
            createPlaybackWaveform();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        LinkedList<float[]> temp = mHistoricalData;
        if (mMode == MODE_RECORDING && temp != null) {
            for (float[] p : temp) {
                canvas.drawLines(p, mWaveFillPaint);
            }
        } else if (mMode == MODE_PLAYBACK) {
            canvas.drawLine(0, centerY, width, centerY, mWaveFillPaint);
            canvas.drawPath(waveformPath, mWaveFillPaint);
            // Marker
            if (markerPosition > -1 && markerPosition <= width && playbackCanvas != null) {
                playbackCanvas.drawLine(0, centerY, /*xStep **/ markerPosition, centerY, mWaveFillPaint);
                playbackCanvas.drawPath(waveformPath, mWaveFillPaint);
                playbackCanvas.drawRect(drawRect.left, 0, /*xStep **/ markerPosition, height, mProgressPaint);
                canvas.drawBitmap(playbackBitmap, 0, 0, mMarkerPaint);
            }
        }
    }

    public int getMode() {
        return mMode;
    }

    public WaveformView setMode(int mMode) {
        this.mMode = mMode;
        return this;
    }

    public short[] getSamples() {
        return mSamples;
    }

    public WaveformView setSamples(short[] samples) {
        mSamples = samples;
        calculateAudioSampleLength();
        onSamplesChanged();
        return this;
    }

    public float getMarkerPosition() {
        return markerPosition;
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    // Progress between 0.0f to 1.0f
    public void setProgress(final float progress) {
        final float position = width * progress;
        handler.post(new Runnable() {
            @Override
            public void run() {
                setMarkerPosition(position);
                postInvalidate();
            }
        });


       /* handler.post(new Runnable() {
            @Override
            public void run() {
                if (animator != null && animator.isRunning()) {
                    animator.cancel();
                }

                animator = ObjectAnimator.ofFloat(this, "markerPosition", markerPosition, position);
                animator.setDuration(1000 / 3);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        postInvalidate();
                    }
                });
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setMarkerPosition(position);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                animator.start();
            }
        });*/
    }


    public void setMarkerPosition(float markerPosition) {
        this.markerPosition = markerPosition;
    }

    public int getAudioLength() {
        return mAudioLength;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public WaveformView setSampleRate(int sampleRate) {
        mSampleRate = sampleRate;
        calculateAudioSampleLength();
        return this;
    }

    public int getChannels() {
        return mChannels;
    }

    public WaveformView setChannels(int channels) {
        mChannels = channels;
        calculateAudioSampleLength();
        return this;
    }

    private void calculateAudioSampleLength() {
        if (mSamples == null || mSampleRate == 0 || mChannels == 0) {
            return;
        }
        mAudioLength = Utils.calculateAudioLength(mSamples.length, mSampleRate, mChannels);
    }

    private void onSamplesChanged() {
        if (mMode == MODE_RECORDING) {
            if (mHistoricalData == null) {
                mHistoricalData = new LinkedList<>();
            }
            LinkedList<float[]> temp = new LinkedList<>(mHistoricalData);

            // For efficiency, we are reusing the array of points.
            float[] waveformPoints;
            if (temp.size() == HISTORY_SIZE) {
                waveformPoints = temp.removeFirst();
            } else {
                waveformPoints = new float[width * 4];
            }

            drawRecordingWaveform(mSamples, waveformPoints);
            temp.addLast(waveformPoints);
            mHistoricalData = temp;
            postInvalidate();
        } else if (mMode == MODE_PLAYBACK) {
            markerPosition = Long.MIN_VALUE;
            xStep = width / (mAudioLength * 1.0f);
            createPlaybackWaveform();
        }
    }

    private void drawRecordingWaveform(short[] buffer, float[] waveformPoints) {
        float lastX = -1;
        float lastY = -1;
        int pointIndex = 0;
        float max = Short.MAX_VALUE;

        // For efficiency, we don't draw all of the samples in the buffer, but only the ones
        // that align with pixel boundaries.
        waveformPath.reset();
        waveformPath.moveTo(0, centerY);

        for (int x = 0; x < width; x++) {
            int index = (int) (((x * 1.0f) / width) * buffer.length);
            short sample = buffer[index];
            float y = centerY - ((sample / max) * centerY);

            waveformPath.lineTo(x, y);

            if (lastX != -1) {
                waveformPoints[pointIndex++] = lastX;
                waveformPoints[pointIndex++] = lastY;
                waveformPoints[pointIndex++] = x;
                waveformPoints[pointIndex++] = y;
            }

            lastX = x;
            lastY = y;
        }

        waveformPath.close();
    }

    private Path drawPlaybackWaveform(int width, int height, short[] buffer) {
        waveformPath.reset();

        final int space = 5;
        float centerY = height / 2f;
        float max = Short.MAX_VALUE;

        short[][] extremes = getExtremes(buffer, width);

        waveformPath.moveTo(0, centerY);
        // draw maximums
        for (int x = 0; x < width - space; x += space) {
            short sample = extremes[x][0];
            float y = centerY - ((sample / max) * centerY);

            waveformPath.lineTo(x, y);
            waveformPath.lineTo(x + space - 1, y);
            waveformPath.lineTo(x + space - 1, height - y);
            waveformPath.lineTo(x, height - y);
            waveformPath.lineTo(x, centerY);
            waveformPath.moveTo(x + space, centerY);
        }

        waveformPath.moveTo(width, centerY);

        // draw minimums
        /*for (int x = width - 1; x >= space; x -= space) {
            short sample = extremes[x][1];
            float y = centerY - ((sample / max) * centerY);

            waveformPath.lineTo(x, y);
            waveformPath.lineTo(x - space + 1, y);
            waveformPath.lineTo(x - space + 1, centerY);
            waveformPath.lineTo(x - space, centerY);
        }

        waveformPath.moveTo(0, centerY);*/
        waveformPath.close();

        return waveformPath;
    }

    private void createPlaybackWaveform() {
        if (width <= 0 || height <= 0 || mSamples == null) {
            return;
        }

        playbackBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        playbackCanvas = new Canvas(playbackBitmap);

        Canvas cacheCanvas;
        if (Build.VERSION.SDK_INT >= 23 && isHardwareAccelerated()) {
            mCachedWaveform = new Picture();
            cacheCanvas = mCachedWaveform.beginRecording(width, height);
        } else {
            mCachedWaveformBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cacheCanvas = new Canvas(mCachedWaveformBitmap);
        }

        Path mWaveform = drawPlaybackWaveform(width, height, mSamples);
        cacheCanvas.drawPath(mWaveform, mWaveFillPaint);
        drawAxis(cacheCanvas, width);

        if (mCachedWaveform != null) {
            mCachedWaveform.endRecording();
        }
        postInvalidate();
    }

    private void drawAxis(Canvas canvas, int width) {
        if (!showTextAxis) {
            return;
        }
        int seconds = mAudioLength / 1000;
        float xStep = width / (mAudioLength / 1000f);
        float textHeight = mTextPaint.getTextSize();
        float textWidth = mTextPaint.measureText("10.00");
        int secondStep = (int) (textWidth * seconds * 2) / width;
        secondStep = Math.max(secondStep, 1);
        for (float i = 0; i <= seconds; i += secondStep) {
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", i), i * xStep, textHeight, mTextPaint);
        }
    }

    public void reset() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        markerPosition = -1f;
        playbackBitmap = null;
        playbackCanvas = null;
        onSamplesChanged();
    }
}
