package com.brotherpowers.audiojournal.View;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * Created by harsh_v on 10/14/16.
 */

public class RecyclerviewDecor extends RecyclerView.ItemDecoration {

    private Paint paint;

    public RecyclerviewDecor() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = 4;
    }


    private RectF getBounds(View view) {
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();

        int left = view.getPaddingLeft();
        int right = view.getWidth() - view.getPaddingRight();

        int top = view.getTop() - layoutParams.topMargin;
        int bottom = view.getBottom() + layoutParams.bottomMargin;

        return new RectF(left, top, right, bottom);
    }
}
