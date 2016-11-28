package com.brotherpowers.hvprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by harsh_v on 11/22/16.
 */

public class ProgressView extends View {
    private static final int MARGIN_HORIZONTAL = 16;
    private static final int MARGIN_VERTICAL = 16;
//    private static final int RANGE = 200;


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

    private Paint mTextPaint;
    private Paint mProgressPaint;
    private Paint mBackgroundPaint;
    private RectF bounds;

    private RectF innerBounds;
    int innerBoundsdiff = 56;
    private float progressWith;
    private float startAngle;
    private float endAngle;
    private float sweepAngle;

    private String text = "00.00";
    private Drawable drawableKnob;


    private void init(Context context, AttributeSet attrs, int defStyle) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, defStyle, 0);
        int progressColor = array.getColor(R.styleable.ProgressView_progressColor, ContextCompat.getColor(context, R.color.progressColor));
        int backgroundColor = array.getColor(R.styleable.ProgressView_backgroundColor, ContextCompat.getColor(context, R.color.backgroundColor));
        int textColor = array.getColor(R.styleable.ProgressView_textColor, ContextCompat.getColor(context, R.color.textColor));

        float defaultTextSize = context.getResources().getDimensionPixelSize(R.dimen.defaultTextSize);
        float textSize = array.getDimension(R.styleable.ProgressView_textSize, defaultTextSize);

        int defaultWidth = context.getResources().getDimensionPixelSize(R.dimen.defaultProgressWidth);
        progressWith = array.getDimension(R.styleable.ProgressView_progressWidth, defaultWidth);

        float startOffset = context.getResources().getInteger(R.integer.startAngle);
        startAngle = array.getFloat(R.styleable.ProgressView_startAngle, startOffset);

        float endOffset = context.getResources().getInteger(R.integer.endAngle);
        endAngle = array.getFloat(R.styleable.ProgressView_endAngle, endOffset);

        sweepAngle = array.getFloat(R.styleable.ProgressView_sweepAngle, 0f);

        array.recycle();


        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

        drawableKnob = ContextCompat.getDrawable(context, R.drawable.ic_knob);

        bounds = new RectF();
        innerBounds = new RectF();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int min = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(min, min);
    }

    private float marginHorizontal() {
        return MARGIN_HORIZONTAL + progressWith;
    }

    private float marginVertical() {
        return MARGIN_VERTICAL + progressWith;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bounds.set(marginHorizontal(), marginVertical(), w - marginHorizontal(), h - marginVertical());

        innerBounds.set(bounds.left + innerBoundsdiff, bounds.top + innerBoundsdiff, bounds.right - innerBoundsdiff, bounds.bottom - innerBoundsdiff);
    }

    final int knobRadius = 16;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        {
            float r1 = (bounds.width() / 2);
            float r2 = r1 - 25;

            for (float degree = startAngle; degree < startAngle + endAngle; degree += 1.0f) {

                double angle = (degree * (Math.PI / 180));

                float x1 = (float) (r1 * Math.cos(angle) + (bounds.centerX()));
                float y1 = (float) (r1 * Math.sin(angle) + (bounds.centerY()));

                float x2 = (float) (r2 * Math.cos(angle) + (bounds.centerX()));
                float y2 = (float) (r2 * Math.sin(angle) + (bounds.centerY()));

                mBackgroundPaint.setStrokeWidth(1f);
                canvas.drawLine(x1, y1, x2, y2, mBackgroundPaint);

            }
        }

        //      background arc
//        canvas.drawArc(bounds, startAngle, endAngle, false, mBackgroundPaint);

//        canvas.drawOval(bounds, mBackgroundPaint);
//        mBackgroundPaint.setStrokeWidth(8);


//        canvas.drawArc(innerBounds, startAngle, sweepAngle, false, mProgressPaint);

        canvas.drawText(text, bounds.centerX(), bounds.centerY() - mTextPaint.getTextSize(), mTextPaint);

        double angle = ((startAngle + sweepAngle) * (Math.PI / 180));
        float radius = (bounds.width() / 2) - innerBoundsdiff; // radius

        int x1 = (int) (radius * Math.cos(angle) + bounds.centerX());
        int y1 = (int) (radius * Math.sin(angle) + bounds.centerY());

        canvas.drawLine(bounds.centerX(), bounds.centerY(), x1, y1, mBackgroundPaint);

        drawableKnob.setBounds((int) (bounds.centerX() - knobRadius), (int) (bounds.centerY() - knobRadius), (int) (bounds.centerX() + knobRadius), (int) (bounds.centerY() + knobRadius));
        drawableKnob.draw(canvas);

//        canvas.drawBitmap(drawableKnob, x1, y1, 20, mBackgroundPaint);

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
}
