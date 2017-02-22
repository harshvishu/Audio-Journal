package com.brotherpowers.audiojournal.TextEditor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import io.github.mthli.knife.KnifeText;

/**
 * Created by harsh_v on 2/17/17.
 */

public class LineKnifeTextView extends KnifeText {
    private Rect _rect;
    private Paint _paint;

    public LineKnifeTextView(Context context) {
        super(context);
        init();
    }

    public LineKnifeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineKnifeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LineKnifeTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        _rect = new Rect();
        _paint = new Paint();
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setColor(getCurrentHintTextColor());
    }

    @Override
    public void draw(Canvas canvas) {
        int count = getLineCount();

        for (int i = 0; i < count; i++) {
            int baseLine = getLineBounds(i, _rect);
            canvas.drawLine(_rect.left, baseLine + 1, _rect.right, baseLine + 1, _paint);
        }
        super.draw(canvas);

    }
}
