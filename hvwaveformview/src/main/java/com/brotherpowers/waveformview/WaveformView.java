package com.brotherpowers.waveformview;

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

    private Paint textPaint;
    private Paint waveFillPaint;
    private Paint markerPaint;
    protected Paint progressPaint;

    private int mode;
    private int audioLength;
    private float markerPosition;
    private int sampleRate;
    private int channels;
    private short[] samples;

//    private boolean showTextAxis = true;

    private Path waveformPath;
    @SuppressWarnings("FieldCanBeLocal")
    private Bitmap cachedWaveformBitmap;
    private Picture cachedWaveform;
    private Bitmap playbackBitmap;
    private Canvas playbackCanvas;
    private Rect drawRect;
    private LinkedList<float[]> historicalData;


    private int width;
    private int height;
    private float xStep;
    private float centerY;


    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveformView, defStyle, 0);

        final int waveFillColor = array.getColor(R.styleable.WaveformView_waveFillColor, ContextCompat.getColor(context, R.color.wave));
        final int markerColor = array.getColor(R.styleable.WaveformView_markerColor, ContextCompat.getColor(context, R.color.marker));
        final int textColor = array.getColor(R.styleable.WaveformView_timeCodeColor, ContextCompat.getColor(context, R.color.text));

        mode = array.getInt(R.styleable.WaveformView_mode, MODE_PLAYBACK);
        array.recycle();

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(textColor);
        textPaint.setTextSize(Utils.getFontSize(context, android.R.attr.textAppearance));

        waveFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waveFillPaint.setStyle(Paint.Style.FILL);
        waveFillPaint.setColor(waveFillColor);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setColor(markerColor);
        progressPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setColor(markerColor);

        waveformPath = new Path();
        historicalData = new LinkedList<>();

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = getMeasuredWidth();
        height = getMeasuredHeight();
        xStep = width / (audioLength * 1.0f);
        centerY = height / 2f;
        drawRect = new Rect(0, 0, width, height);

        if (historicalData != null) {
            historicalData.clear();
        }
        if (mode == MODE_PLAYBACK) {
            createPlaybackWaveform();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        LinkedList<float[]> temp = historicalData;
        if (mode == MODE_RECORDING && temp != null) {
            for (float[] p : temp) {
                canvas.drawLines(p, waveFillPaint);
            }
        } else if (mode == MODE_PLAYBACK) {
            canvas.drawLine(0, centerY, width, centerY, waveFillPaint);
            canvas.drawPath(waveformPath, waveFillPaint);
            // Marker
            if (markerPosition > -1 && markerPosition <= width && playbackCanvas != null) {
                playbackCanvas.drawLine(0, centerY, /*xStep **/ markerPosition, centerY, waveFillPaint);
                playbackCanvas.drawPath(waveformPath, waveFillPaint);
                playbackCanvas.drawRect(drawRect.left, 0, /*xStep **/ markerPosition, height, progressPaint);
                canvas.drawBitmap(playbackBitmap, 0, 0, markerPaint);
            }
        }
    }

    public int getMode() {
        return mode;
    }

    public WaveformView setMode(int mMode) {
        this.mode = mMode;
        return this;
    }

    public short[] getSamples() {
        return samples;
    }

    public WaveformView setSamples(short[] samples) {
        this.samples = samples;
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
    }


    public void setMarkerPosition(float markerPosition) {
        this.markerPosition = markerPosition;
    }

    public int getAudioLength() {
        return audioLength;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public WaveformView setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        calculateAudioSampleLength();
        return this;
    }

    public int getChannels() {
        return channels;
    }

    public WaveformView setChannels(int channels) {
        this.channels = channels;
        calculateAudioSampleLength();
        return this;
    }

    private void calculateAudioSampleLength() {
        if (samples == null || sampleRate == 0 || channels == 0) {
            return;
        }
        audioLength = Utils.calculateAudioLength(samples.length, sampleRate, channels);
    }

    private void onSamplesChanged() {
        if (mode == MODE_RECORDING) {
            if (historicalData == null) {
                historicalData = new LinkedList<>();
            }
            LinkedList<float[]> temp = new LinkedList<>(historicalData);

            // For efficiency, we are reusing the array of points.
            float[] waveformPoints;
            if (temp.size() == HISTORY_SIZE) {
                waveformPoints = temp.removeFirst();
            } else {
                waveformPoints = new float[width * 4];
            }

            drawRecordingWaveform(samples, waveformPoints);
            temp.addLast(waveformPoints);
            historicalData = temp;
            postInvalidate();
        } else if (mode == MODE_PLAYBACK) {
            markerPosition = Long.MIN_VALUE;
            xStep = width / (audioLength * 1.0f);
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
        if (width <= 0 || height <= 0 || samples == null) {
            return;
        }

        playbackBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        playbackCanvas = new Canvas(playbackBitmap);

        Canvas cacheCanvas;
        if (Build.VERSION.SDK_INT >= 23 && isHardwareAccelerated()) {
            cachedWaveform = new Picture();
            cacheCanvas = cachedWaveform.beginRecording(width, height);
        } else {
            cachedWaveformBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            cacheCanvas = new Canvas(cachedWaveformBitmap);
        }

        Path mWaveform = drawPlaybackWaveform(width, height, samples);
        cacheCanvas.drawPath(mWaveform, waveFillPaint);

//        drawAxis(cacheCanvas, width);

        if (cachedWaveform != null) {
            cachedWaveform.endRecording();
        }
        postInvalidate();
    }

    /*private void drawAxis(Canvas canvas, int width) {
        if (!showTextAxis) {
            return;
        }
        int seconds = audioLength / 1000;
        float xStep = width / (audioLength / 1000f);
        float textHeight = textPaint.getTextSize();
        float textWidth = textPaint.measureText("10.00");
        int secondStep = (int) (textWidth * seconds * 2) / width;
        secondStep = Math.max(secondStep, 1);
        for (float i = 0; i <= seconds; i += secondStep) {
            canvas.drawText(String.format(Locale.getDefault(), "%.2f", i), i * xStep, textHeight, textPaint);
        }
    }*/

    public void reset() {
        markerPosition = Long.MIN_VALUE;
        playbackBitmap = null;
        playbackCanvas = null;
        onSamplesChanged();
    }
}
