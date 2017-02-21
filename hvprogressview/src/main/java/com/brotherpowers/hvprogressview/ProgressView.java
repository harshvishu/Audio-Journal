package com.brotherpowers.hvprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static com.brotherpowers.hvprogressview.Utils.Defaults.TEXT;

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

    private TextPaint mTextPaint;
    private Paint mProgressPaint;
    private Paint mBackgroundPaint;
    private Paint mMeterPaint;
    private RectF bounds;

    private float progressWith;
    private float startAngle;
    private float endAngle;
    private float sweepAngle;           // Current angle from progress
    private float knobOffset;           // Offset for Knob
    private int knobRadius;
    private int max = 100;

    private String text = TEXT;
    private Drawable drawableKnob;

    private float initialSweepAngle;

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, defStyle, 0);
        final int progressColor = array.getColor(R.styleable.ProgressView_progressColor, ContextCompat.getColor(context, R.color.progressColor));
        final int backgroundColor = array.getColor(R.styleable.ProgressView_backgroundColor, ContextCompat.getColor(context, R.color.backgroundColor));
        final int textColor = array.getColor(R.styleable.ProgressView_textColor, ContextCompat.getColor(context, R.color.textColor));
        final float progressWidthMultiplier = array.getFloat(R.styleable.ProgressView_progressWidthMultiplier, 1.0f);

        drawableKnob = array.getDrawable(R.styleable.ProgressView_drawable);
        if (drawableKnob == null) {
            drawableKnob = ContextCompat.getDrawable(context, R.drawable.ic_knob);
        }

        knobOffset = array.getFloat(R.styleable.ProgressView_knobOffset, 0.0f);     // knob Offset
        knobRadius = array.getInt(R.styleable.ProgressView_knobRadius, 24);         // knob Radius

        int defaultTextSize = context.getResources().getDimensionPixelSize(R.dimen.defaultTextSize);
        int textSize = array.getDimensionPixelSize(R.styleable.ProgressView_textSize, defaultTextSize);

        int defaultWidth = context.getResources().getDimensionPixelSize(R.dimen.defaultProgressWidth);
        progressWith = array.getDimensionPixelSize(R.styleable.ProgressView_progressWidth, defaultWidth);

        float startOffset = context.getResources().getInteger(R.integer.startAngle);
        startAngle = array.getFloat(R.styleable.ProgressView_startAngle, startOffset);

        float endOffset = context.getResources().getInteger(R.integer.endAngle);
        endAngle = array.getFloat(R.styleable.ProgressView_endAngle, endOffset);

        initialSweepAngle = array.getFloat(R.styleable.ProgressView_sweepAngle, 0f);
        sweepAngle = initialSweepAngle;
        array.recycle();


        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

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
        mProgressPaint.setShader(new SweepGradient(bounds.centerX(), bounds.centerY(), Color.parseColor("#DC592F"), Color.parseColor("#D74265")));
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        double angle = ((startAngle + sweepAngle) * (Math.PI / 180)); // Angle
        float radius = (bounds.width() / 2) - knobOffset; // Radius


        drawMeteredLines(canvas, radius);

        drawCurves(canvas);

        drawKnob(canvas, angle, radius);

        drawText(canvas, angle, radius);

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
//        canvas.drawArc(bounds, startAngle, endAngle, false, mBackgroundPaint);
        // draw

//        canvas.drawOval(bounds, mBackgroundPaint);
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
        drawableKnob.draw(canvas);
    }

    /**
     * Draw text
     */
    private void drawText(Canvas canvas, double angle, float radius) {
        float offset = radius - knobRadius - Utils.Defaults.TEXT_OFFSET - mTextPaint.getTextSize();
        int x = (int) (offset * Math.cos(angle) + bounds.centerX());
        int y = (int) (offset * Math.sin(angle) + bounds.centerY());
        canvas.drawText(text, x, y, mTextPaint);
    }

    public void setText(String text) {
        this.text = text;
        requestLayout();
    }


    public void setMax(int max) {
        this.max = max;
    }

    public void setProgress(float progress, @Nullable String text) {
        if (!TextUtils.isEmpty(text)) {
            this.text = text;
        }
        this.sweepAngle = calculateAngle(progress);

        postInvalidate();
    }

    /**
     * @param progress current progress between 0 to max
     * @return angle by calculating the percentage
     */
    private float calculateAngle(float progress) {
        return endAngle * progress / max;
    }

    public void reset() {
        setProgress(0f, TEXT);
    }
}
