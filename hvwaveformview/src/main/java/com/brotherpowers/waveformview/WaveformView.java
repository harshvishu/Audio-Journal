package com.brotherpowers.waveformview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

/**
 * Created by harsh_v on 11/21/16.
 */

public class WaveformView extends View {
    private static final int HISTORY_SIZE = 2;
    public static final int MODE_RECORDING = 1;
    public static final int MODE_PLAYBACK = 2;
    private static final int MAX_TRACK_DURATION = 60_000;

    //internal margin
    private static final int LAYOUT_MARGIN_HORIZONTAL = 16;
    private static final int LAYOUT_MARGIN_VERTICAL = 16;

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
    private Paint mWaveStrokePaint;
    private Paint mMarkerPaint;
    private Paint mPointerPaint;
    private int mMode;


    private int mAudioLength = MAX_TRACK_DURATION; // fixed for 60 seconds

    private LinkedList<float[]> historicalData;
    private short[] samples;
    private float markerPosition;
    private Path path;
    private Drawable leftMarker;


    private void init(Context context, AttributeSet attrs, int defStyle) {
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveformView, defStyle, 0);

        final float strokeWidth = array.getFloat(R.styleable.WaveformView_strokeWidth, 1f);

        final int waveFillColor = array.getColor(R.styleable.WaveformView_waveFillColor, ContextCompat.getColor(context, R.color.waveFill));
        final int waveStrokeColor = array.getColor(R.styleable.WaveformView_waveStrokeColor, ContextCompat.getColor(context, R.color.waveStroke));
        final int markerColor = array.getColor(R.styleable.WaveformView_markerColor, ContextCompat.getColor(context, R.color.marker));
        final int pointerColor = array.getColor(R.styleable.WaveformView_pointerColor, ContextCompat.getColor(context, R.color.pointer));
        final int textColor = array.getColor(R.styleable.WaveformView_timeCodeColor, ContextCompat.getColor(context, R.color.text));

        mMode = array.getInt(R.styleable.WaveformView_mode, MODE_PLAYBACK);
        array.recycle();

        leftMarker = ContextCompat.getDrawable(context, R.drawable.ic_circle_fill);
        leftMarker.setColorFilter(waveFillColor, PorterDuff.Mode.SRC);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(16f/*Utils.getFontSize(context, android.R.attr.textAppearance)*/);

        mWaveStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWaveStrokePaint.setStyle(Paint.Style.STROKE);
        mWaveStrokePaint.setStrokeWidth(strokeWidth);
        mWaveStrokePaint.setColor(waveStrokeColor);

        mWaveFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWaveFillPaint.setStyle(Paint.Style.FILL);
        mWaveFillPaint.setColor(waveFillColor);

        mMarkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMarkerPaint.setStyle(Paint.Style.STROKE);
        mMarkerPaint.setStrokeWidth(strokeWidth);
        mMarkerPaint.setColor(markerColor);

        mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerPaint.setStyle(Paint.Style.FILL);
        mPointerPaint.setColor(pointerColor);


        historicalData = new LinkedList<>();

        path = new Path();

        /*if (mMode == MODE_PLAYBACK) {
            createPlaybackWaveform();
        }*/
    }

    private int width;
    private int height;
    private float centerY;
    private float xStep;
    private Point origin;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Rect rect = new Rect(LAYOUT_MARGIN_HORIZONTAL, LAYOUT_MARGIN_VERTICAL,
                w - LAYOUT_MARGIN_HORIZONTAL, h - LAYOUT_MARGIN_VERTICAL);

        width = rect.width();
        height = rect.height();

        origin = new Point(rect.left, rect.top);

        centerY = rect.centerY(); // vertical mid

        historicalData.clear();

        onSamplesChanged();
    }


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawLine(0, centerY, getWidth(), centerY, mMarkerPaint);

        if (clipPath != null && clipView) {
            canvas.clipPath(clipPath, Region.Op.INTERSECT);
        }

        canvas.drawPath(path, mWaveFillPaint);

//        canvas.drawCircle(pointerRadius, centerY, pointerRadius, mPointerPaint);

//        canvas.drawCircle(getWidth() - pointerRadius, centerY, pointerRadius, mPointerPaint);

        if (mMode == MODE_PLAYBACK) {
            drawAxis(canvas, width);
        }
    }

    public WaveformView setSamples(short[] samples) {
        this.samples = samples;
        onSamplesChanged();
        return this;
    }


    public WaveformView setAudioLength(int mAudioLength) {
        this.mAudioLength = mAudioLength;
        onSamplesChanged();
        return this;
    }

    private void onSamplesChanged() {
        if (samples == null || width == 0) {
            return;
        }

        calculateXStep();

        if (mMode == MODE_PLAYBACK) {
            createPlaybackWaveForm();
        } else {
            createRecordingWaveForm();
//            createClipPath();
//            createPlaybackWaveForm();
        }

        postInvalidate();


    }


    private float dampningMultiplier = 1f;

    private void createRecordingWaveForm() {
        final float max = Short.MAX_VALUE;
        final long fps = 1000 / 30;


        float lastX = -1;
        float lastY = -1;

        path.reset();
        path.moveTo(getLeft(), height);
        path.lineTo(getLeft(), centerY);

        // For efficiency, we don't draw all of the samples in the buffer, but only the ones
        // that align with pixel boundaries.
        if (width > 3) {
            for (float x = 0; x < width - 3; x += 3.0f) {
                int index = (int) ((x / width) * samples.length);
                short sample = samples[index];
                float y = centerY - ((sample / max) * centerY * (1 / dampningMultiplier) - LAYOUT_MARGIN_VERTICAL);

                // Add the initial points
                if (lastX == -1) {
                    path.lineTo(x, y);
                } else {
                    path.quadTo(lastX, lastY, x, y);
                }

                lastX = x;
                lastY = y;
            }
        }

        path.lineTo(width, centerY);
        path.lineTo(lastX, height);
        path.close();

    }

    private void createPlaybackWaveForm() {
        float max = Short.MAX_VALUE;

        path.reset();
        path.moveTo(origin.x, centerY);

        // For efficiency, we don't draw all of the samples in the buffer, but only the ones
        // that align with pixel boundaries.
        float spacing = 3.0f;
        if (width > spacing) {
            for (float x = 0; x < width - spacing; x += spacing) {
                int index = (int) ((x / width) * samples.length);
                short sample = samples[index];
                float y = centerY - ((sample / max) * (centerY - LAYOUT_MARGIN_VERTICAL));

                path.lineTo(x, y);
                path.lineTo(x + spacing - 1, y);
                path.lineTo(x + spacing - 1, centerY);
                path.moveTo(x + spacing, centerY);
            }
        }

//        path.lineTo(width, centerY);
        path.close();
    }

    @SuppressLint("DefaultLocale")
    private void drawAxis(Canvas canvas, int width) {
        int seconds = mAudioLength / 1000;
        float textHeight = mTextPaint.getTextSize();
        float textWidth = mTextPaint.measureText("10.00");
        int secondStep = (int) (textWidth * seconds * 2) / width;
        secondStep = Math.max(secondStep, 1);
        for (float i = 0; i <= seconds; i += secondStep) {
            canvas.drawText(String.format("%.2f", i), i * xStep + LAYOUT_MARGIN_HORIZONTAL, textHeight, mTextPaint);
        }
    }


    private void calculateXStep() {
        xStep = (float) width / (mAudioLength / 1000f);
    }

    public void setSamples(short[] samples, int duration) {
        this.mAudioLength = duration;
        this.samples = samples;
        onSamplesChanged();
    }

    private boolean clipView;

    public void enableClipping(boolean clip) {
        this.clipView = clip;
    }

    private Path clipPath;

    private void createClipPath() {
        clipPath = new Path();
        clipPath.moveTo(origin.x, centerY);
        clipPath.lineTo(origin.x, origin.y);
        clipPath.lineTo(width, origin.y);
        clipPath.lineTo(width, centerY);
        clipPath.cubicTo(width, centerY, width / 2, height, origin.x, centerY);
        clipPath.close();
    }

}
