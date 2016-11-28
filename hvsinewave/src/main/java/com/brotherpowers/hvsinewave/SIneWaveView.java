package com.brotherpowers.hvsinewave;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by harsh_v on 11/24/16.
 */

public class SineWaveView extends View {
    public SineWaveView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public SineWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public SineWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private Paint mWavePaint;
    private Rect drawRect;

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SineWaveView, defStyleAttr, 0);

        float strokeWidth = array.getFloat(R.styleable.SineWaveView_strokeWidth, 2f);
        int strokeColor = array.getColor(R.styleable.SineWaveView_strokeColor, ContextCompat.getColor(context, R.color.strokeColor));
        int lines = array.getInteger(R.styleable.SineWaveView_lines, 2);

        array.recycle();

        mWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setStrokeWidth(strokeWidth);
        mWavePaint.setColor(strokeColor);

        drawRect = new Rect();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        drawRect.set(0, 0, w, h);

        amplitude = drawRect.height() / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        ArrayList<Point> points = new ArrayList<>();

        Path path = new Path();

        verticalShift = drawRect.centerY();

        int lastX = drawRect.left;
        int lastY = drawRect.centerY();

        path.moveTo(lastX, lastY);

        for (int x = drawRect.left; x < drawRect.width(); x += 3) {
            int y = (int) (amplitude * Math.sin(x * (1 / 10d)) + verticalShift);

            path.quadTo(lastX, lastY, x, y);

            lastX = x;
            lastY = y;

        }

        path.close();
        canvas.drawPath(path, mWavePaint);


    }

    int amplitude;
    double phaseDiff = 2 * Math.PI; // Cycle
    int phaseShift = 0;             // Displacement
    int verticalShift = 0;          // Vertical Displacement
}
