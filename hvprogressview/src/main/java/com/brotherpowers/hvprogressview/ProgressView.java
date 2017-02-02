package com.brotherpowers.hvprogressview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateInterpolator;

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
    private int knobRadius = 16;

    private String text = "00:00:00";
    private Drawable drawableKnob;


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

        sweepAngle = array.getFloat(R.styleable.ProgressView_sweepAngle, 0f);
        array.recycle();


        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setShader(new SweepGradient(5, 5, new int[]{Color.RED, Color.BLUE, Color.GREEN}, new float[]{0.5f, 0.3f, 0.2f}));
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        mProgressPaint.setColor(progressColor);
        mProgressPaint.setStrokeWidth(progressWith);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackgroundPaint.setColor(backgroundColor);
        mBackgroundPaint.setStrokeWidth(progressWith);


//        drawableKnob = ContextCompat.getDrawable(context, R.drawable.ic_knob);

        bounds = new RectF();



        ObjectAnimator animator = ObjectAnimator.ofInt(progressWith, "backgroundColor", Color.WHITE, Color.RED);
//        animator.setRepeatCount(ObjectAnimator.INFINITE);
//        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(5000);
        animator.start();
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
        /*{
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
        }*/

        //      background arc
        canvas.drawArc(bounds, startAngle, endAngle, false, mBackgroundPaint);
        canvas.drawOval(bounds, mBackgroundPaint);

        mBackgroundPaint.setStrokeWidth(8);
        canvas.drawArc(bounds, startAngle, sweepAngle, false, mProgressPaint);

        drawText(canvas);

        double angle = ((startAngle + sweepAngle) * (Math.PI / 180)); // Angle
        float radius = (bounds.width() / 2) - knobOffset; // Radius

        int x1 = (int) (radius * Math.cos(angle) + bounds.centerX());
        int y1 = (int) (radius * Math.sin(angle) + bounds.centerY());

//        canvas.drawLine(bounds.centerX(), bounds.centerY(), x1, y1, mBackgroundPaint);

        drawableKnob.setBounds(x1 - knobRadius, y1 - knobRadius, x1 + knobRadius, y1 + knobRadius);
        drawableKnob.draw(canvas);


        int textX = (int) ((radius - 50 - mTextPaint.getTextSize()) * Math.cos(angle) + bounds.centerX());
        int textY = (int) ((radius - 50 - mTextPaint.getTextSize()) * Math.sin(angle) + bounds.centerY());

        canvas.drawText(text, textX, textY, mTextPaint);


/*

        canvas.drawPicture(new Picture());

        RectF sineRect = new RectF(bounds.left + 10, bounds.centerY() + 10, bounds.right - 10, bounds.bottom - 10);
        System.out.println(sineRect);

        float a, w, h, x, y;
        w = sineRect.width();
        h = sineRect.height();

        x = sineRect.left;
        canvas.drawLine(x, sineRect.centerY(), (int) w, sineRect.centerY(), mBackgroundPaint);//Draw a line from left to right as the y=0 line.
        while (x < w) {
            a = (x / w) * ((float) 2.0 * (float) 3.131592654);
            y = (sineRect.centerY()) - ((float) Math.sin(2 * a) * (sineRect.centerY() / (float) 2.1));

            canvas.drawPoint(x, y, mBackgroundPaint);
            x++;
        }*/


//        canvas.drawBitmap(drawableKnob, x1, y1, 20, mBackgroundPaint);

    }

    private void drawText(Canvas canvas) {

       /* Rect texBounds = new Rect();
        mTextPaint.getTextBounds(text, 0, "77.77".length(), texBounds);

        texBounds.left = (int) (texBounds.left + bounds.left);
        texBounds.top = (int) (texBounds.top + bounds.top);
        texBounds.right = (int) (texBounds.right + bounds.right);
        texBounds.bottom = (int) (texBounds.bottom + bounds.bottom);

        StaticLayout textStaticLayout = new StaticLayout(text, mTextPaint, texBounds.width(), Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);

        canvas.save();
        canvas.clipRect(texBounds);
        canvas.translate(texBounds.left, (texBounds.centerY() - textStaticLayout.getHeight() / 2f));
        textStaticLayout.draw(canvas);
        canvas.restore();*/
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
