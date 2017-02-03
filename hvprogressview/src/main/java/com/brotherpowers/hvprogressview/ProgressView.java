package com.brotherpowers.hvprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

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
    private RectF bounds;

    private float progressWith;
    private float startAngle;
    private float endAngle;
    private float sweepAngle;           // Current angle from progress
    private float knobOffset;           // Offset for Knob
    private int knobRadius;

    private String text = TEXT;
    private Drawable drawableKnob;

    private float initialSweepAngle;

    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, defStyle, 0);
        int progressColor = array.getColor(R.styleable.ProgressView_progressColor, ContextCompat.getColor(context, R.color.progressColor));
        int backgroundColor = array.getColor(R.styleable.ProgressView_backgroundColor, ContextCompat.getColor(context, R.color.backgroundColor));
        int textColor = array.getColor(R.styleable.ProgressView_textColor, ContextCompat.getColor(context, R.color.textColor));

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
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setStrokeWidth(progressWith);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackgroundPaint.setColor(backgroundColor);
        mBackgroundPaint.setStrokeWidth(progressWith);

        bounds = new RectF();

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
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        double angle = ((startAngle + sweepAngle) * (Math.PI / 180)); // Angle
        float radius = (bounds.width() / 2) - knobOffset; // Radius

        drawCurves(canvas);

        drawKnob(canvas, angle, radius);

        drawText(canvas, angle, radius);

    }

    /**
     * Draw Arc for progress & background
     */
    private void drawCurves(Canvas canvas) {
//        canvas.drawArc(bounds, startAngle, endAngle, false, mBackgroundPaint);
        canvas.drawOval(bounds, mBackgroundPaint);
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


    private int max = 100;

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
        sweepAngle = initialSweepAngle;
        text = TEXT;
    }
}
