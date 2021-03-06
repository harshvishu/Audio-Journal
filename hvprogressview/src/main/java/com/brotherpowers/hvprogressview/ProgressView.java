package com.brotherpowers.hvprogressview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static com.brotherpowers.hvprogressview.Utils.Defaults.TEXT_HEADER;
import static com.brotherpowers.hvprogressview.Utils.Defaults.TEXT_START_TIME;

/**
 * Created by harsh_v on 11/22/16.
 */

public class ProgressView extends View {


    public ProgressView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private TextPaint counterTextPaint;
    private TextPaint headerTextPaint;
    private Paint mProgressPaint;
    private Paint mBackgroundPaint;
    private Paint mMeterPaint;
    private RectF bounds;

    private float progressWith;
    private float startAngle = -90;
    private float endAngle = 360;
    private float sweepAngle;           // Current angle from progress
    private float knobOffset;           // Offset for Knob
    private float initialSweepAngle = 0f;
    private int knobRadius;
    private int max = 100;
    private int counterTextSize;
    private int headerTextSize;

    private int progressColorStart;
    private int progressColorEnd;

    private Drawable drawableKnob;
    private UIListener uiListener;
    private BoringLayout textLayoutCounter;
    private BoringLayout textLayoutHeader;
    private BoringLayout.Metrics boringMetrics;

    private Defaults defaults;

    private void init(Context context, AttributeSet attrs, int defStyle) {
        defaults = new Defaults(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, defStyle, 0);
        progressColorStart = array.getColor(R.styleable.ProgressView_progressColorStart, ContextCompat.getColor(context, R.color.progressColor));
        progressColorEnd = array.getColor(R.styleable.ProgressView_progressColorEnd, ContextCompat.getColor(context, R.color.progressColor));

        final int backgroundColor = array.getColor(R.styleable.ProgressView_backgroundColor, ContextCompat.getColor(context, R.color.backgroundColor));

        ColorStateList counterTextColor = array.getColorStateList(R.styleable.ProgressView_android_textColor);
        if (counterTextColor == null) {
            counterTextColor = ColorStateList.valueOf(0xFF000000);
        }

        ColorStateList headerTextColor = array.getColorStateList(R.styleable.ProgressView_headerTextColor);
        if (headerTextColor == null) {
            headerTextColor = ColorStateList.valueOf(0xFF000000);
        }


        final float progressWidthMultiplier = array.getFloat(R.styleable.ProgressView_progressWidthMultiplier, 1.0f);

        drawableKnob = array.getDrawable(R.styleable.ProgressView_drawable);
        if (drawableKnob == null) {
            drawableKnob = ContextCompat.getDrawable(context, R.drawable.ic_knob);
        }

        knobOffset = array.getFloat(R.styleable.ProgressView_knobOffset, 0.0f);     // knob Offset
        knobRadius = array.getInt(R.styleable.ProgressView_knobRadius, 24);         // knob Radius

        // Text size for Counter
        counterTextSize = array.getDimensionPixelSize(R.styleable.ProgressView_android_textSize, defaults.counterTextSize);

        // Text size for Header
        headerTextSize = array.getDimensionPixelSize(R.styleable.ProgressView_headerTextSize, defaults.headerTextSize);

        // Progress bar width
        progressWith = array.getDimensionPixelSize(R.styleable.ProgressView_progressWidth, defaults.progressWidth);

        // Start Angle
        startAngle = array.getFloat(R.styleable.ProgressView_startAngle, startAngle);

        // End Angle
        endAngle = array.getFloat(R.styleable.ProgressView_endAngle, endAngle);

        initialSweepAngle = array.getFloat(R.styleable.ProgressView_sweepAngle, initialSweepAngle);
        sweepAngle = initialSweepAngle;
        array.recycle();


        counterTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        counterTextPaint.setColor(counterTextColor.getColorForState(new int[]{android.R.attr.state_active}, counterTextColor.getDefaultColor()));
        counterTextPaint.setTextAlign(Paint.Align.CENTER);
        counterTextPaint.setTextSize(counterTextSize);

        headerTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        headerTextPaint.setColor(headerTextColor.getColorForState(new int[]{android.R.attr.state_active}, counterTextColor.getDefaultColor()));
        headerTextPaint.setTextAlign(Paint.Align.CENTER);
        headerTextPaint.setTextSize(headerTextSize);

        Paint.FontMetricsInt fontMetricsInt = counterTextPaint.getFontMetricsInt();
        boringMetrics = new BoringLayout.Metrics();
        boringMetrics.ascent = fontMetricsInt.ascent;
        boringMetrics.bottom = fontMetricsInt.bottom;
        boringMetrics.descent = fontMetricsInt.descent;
        boringMetrics.leading = fontMetricsInt.leading;
        boringMetrics.top = fontMetricsInt.top;

        float textWidth = counterTextPaint.measureText(TEXT_START_TIME, 0, TEXT_START_TIME.length());
        textLayoutCounter = new BoringLayout(TEXT_START_TIME, counterTextPaint, (int) textWidth, Layout.Alignment.ALIGN_CENTER,
                1f, 1f, boringMetrics, false, TextUtils.TruncateAt.MIDDLE, (int) textWidth);

        textWidth = headerTextPaint.measureText(TEXT_HEADER, 0, TEXT_HEADER.length());
        textLayoutHeader = new BoringLayout(TEXT_HEADER, headerTextPaint, (int) textWidth, Layout.Alignment.ALIGN_CENTER,
                1f, 1f, boringMetrics, false, TextUtils.TruncateAt.MIDDLE, (int) textWidth);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
        mProgressPaint.setStrokeWidth(progressWith);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeCap(Paint.Cap.SQUARE);
        mBackgroundPaint.setColor(backgroundColor);
        mBackgroundPaint.setStrokeWidth(progressWith * progressWidthMultiplier);
        mBackgroundPaint.setShadowLayer(2.0f, 0.0f, 0.0f, backgroundColor);

        mMeterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMeterPaint.setStyle(Paint.Style.STROKE);
        mMeterPaint.setColor(backgroundColor);
        mMeterPaint.setStrokeWidth(0.33f);

        bounds = new RectF();

        if (SDK_INT >= HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        // Handler Class
        uiListener = new UIListener(this);
    }


    private final static class UIListener extends Handler {
        private final int UPDATE_UI = 0x0;
        private final int SET_PROGRESS = 0x1;

        private final WeakReference<ProgressView> _progressView;

        UIListener(ProgressView progressView) {
            this._progressView = new WeakReference<>(progressView);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI:
                    // Update the UI
                    _progressView.get().invalidate();
                    break;

                case SET_PROGRESS:
                    Bundle bundle = msg.getData();
                    final float progress = bundle.getFloat("progress");
                    final String text = bundle.getString("text_start_time");
                    _progressView.get().setProgress(progress, text);
                    break;
            }
        }

        void update() {
            sendEmptyMessage(UPDATE_UI);
        }

        void changeProgress(String text, float progress) {
            Message message = new Message();
            Bundle bundle = new Bundle();
            bundle.putFloat("progress", progress);
            bundle.putString("text_start_time", text);
            message.setData(bundle);
            message.what = SET_PROGRESS;

            sendMessage(message);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int min = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(min, min);
    }

    @Override
    public int getPaddingLeft() {
        return super.getPaddingLeft() + (int) progressWith;
    }

    @Override
    public int getPaddingRight() {
        return super.getPaddingRight() + (int) progressWith;
    }

    @Override
    public int getPaddingTop() {
        return super.getPaddingTop() + (int) progressWith;
    }

    @Override
    public int getPaddingBottom() {
        return super.getPaddingBottom() + (int) progressWith;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bounds.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());

        SweepGradient sweepGradient = new SweepGradient(bounds.centerX(), bounds.centerY(), progressColorStart, progressColorEnd);
        Matrix matrix = new Matrix();
        matrix.postRotate(startAngle - endAngle);
        sweepGradient.setLocalMatrix(matrix);
        mProgressPaint.setShader(sweepGradient);

        float textWidth = headerTextPaint.measureText(TEXT_HEADER, 0, TEXT_HEADER.length());
        textLayoutHeader.replaceOrMake(TEXT_HEADER, headerTextPaint, (int) textWidth, Layout.Alignment.ALIGN_CENTER,
                1f, 1f, boringMetrics, false, TextUtils.TruncateAt.MIDDLE, (int) textWidth);
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        double angle = ((startAngle + sweepAngle) * (Math.PI / 180)); // Angle
        float radius = (bounds.width() / 2) - knobOffset; // Radius

        drawText(canvas, angle, radius);

        drawMeteredLines(canvas, radius);

        drawCurves(canvas);

        drawKnob(canvas, angle, radius);


        drawRect(bounds, counterTextPaint, canvas, startAngle, 20, 4);
        drawRect(bounds, counterTextPaint, canvas, startAngle + endAngle, 20, 4);
    }

    private void drawMeteredLines(Canvas canvas, float radius) {
        float r1 = radius;
        float r2 = r1 - progressWith;

        for (float degree = startAngle; degree < startAngle + endAngle; degree += mMeterPaint.getStrokeWidth()) {

            double angle = (degree * (Math.PI / 180));

            float x1 = (float) (r1 * Math.cos(angle) + (bounds.centerX()));
            float y1 = (float) (r1 * Math.sin(angle) + (bounds.centerY()));

            float x2 = (float) (r2 * Math.cos(angle) + (bounds.centerX()));
            float y2 = (float) (r2 * Math.sin(angle) + (bounds.centerY()));

            canvas.drawLine(x1, y1, x2, y2, mMeterPaint);

        }
    }

    /**
     * Draw Arc for progress & background
     */
    private void drawCurves(Canvas canvas) {
        canvas.drawArc(bounds, startAngle, endAngle, false, mBackgroundPaint);
        canvas.drawArc(bounds, startAngle, sweepAngle, false, mProgressPaint);
    }

    /**
     * Draw Knob
     */
    private void drawKnob(Canvas canvas, double angle, float radius) {
        int x1 = (int) (radius * Math.cos(angle) + bounds.centerX());
        int y1 = (int) (radius * Math.sin(angle) + bounds.centerY());

        drawableKnob.setBounds(x1 - knobRadius, y1 - knobRadius, x1 + knobRadius, y1 + knobRadius);

    }

    /**
     * Draw custom rect
     */
    private void drawRect(RectF bounds, Paint paint, Canvas canvas, float angleInDegrees, int width, int height) {
        double angle = angleInRad(angleInDegrees); // Angle
        float radius = (bounds.width() / 2); // Radius

        int x1 = (int) (radius * Math.cos(angle) + bounds.centerX());
        int y1 = (int) (radius * Math.sin(angle) + bounds.centerY());

        int saveCount = canvas.getSaveCount();
        canvas.save();
        canvas.rotate(angleInDegrees, x1, y1);
        RectF rectF = new RectF(x1 - width, y1 - height, x1 + width, y1 + height);
        canvas.drawRect(rectF, paint);
        canvas.restoreToCount(saveCount);
    }

    /**
     * Draw text_start_time
     */
    private void drawText(Canvas canvas, double angle, float radius) {
        // Save canvas state
        int saveCount = canvas.getSaveCount();
        canvas.save();

        // translate to X center
        float x = (canvas.getWidth()) / 2;

        // translate Y for textLayoutCounter
        float y = (canvas.getHeight() - textLayoutCounter.getHeight()) / 2;
        canvas.translate(x, y);
        textLayoutCounter.draw(canvas);

        // Translate for textLayoutHeader
        canvas.translate(0, -textLayoutHeader.getHeight());
        textLayoutHeader.draw(canvas);

        // Restore canvas
        canvas.restoreToCount(saveCount);
    }

    public void setMax(int max) {
        this.max = max;
    }

    void setProgress(final float progress, @Nullable String text) {
        if (!TextUtils.isEmpty(text)) {
            counterTextPaint.setTextSize(counterTextSize);
            float textWidth = counterTextPaint.measureText(text, 0, text.length());
            textLayoutCounter.replaceOrMake(text, counterTextPaint, (int) textWidth, Layout.Alignment.ALIGN_CENTER,
                    1f, 1f, boringMetrics, false, TextUtils.TruncateAt.MIDDLE, (int) textWidth);
        }

        this.sweepAngle = calculateAngle(progress);
        uiListener.update();
    }

    void setSweepAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle;
    }

    /**
     * @param progress current progress between 0 to max
     * @return angle by calculating the percentage
     */
    private float calculateAngle(float progress) {
        return endAngle * progress / max;
    }

    public void reset() {
        set(0f, TEXT_START_TIME);
    }

    public void set(final float progress, @Nullable String text) {
        uiListener.changeProgress(text, progress);
    }

    private double angleInRad(float angleInDegrees) {
        return ((angleInDegrees) * (Math.PI / 180));
    }

    private static class Defaults {
        final int progressWidth;
        final int counterTextSize;
        final int headerTextSize;

        private Defaults(Context context) {
            this.progressWidth = context.getResources().getDimensionPixelSize(R.dimen.defaultProgressWidth);
            counterTextSize = context.getResources().getDimensionPixelSize(R.dimen.defaultCounterTextSize);
            headerTextSize = context.getResources().getDimensionPixelSize(R.dimen.defaultHeaderTextSize);
        }
    }
}
