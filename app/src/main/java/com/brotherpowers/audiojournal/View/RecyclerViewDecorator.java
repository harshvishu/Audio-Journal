package com.brotherpowers.audiojournal.View;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by harsh_v on 2/16/17.
 */

public class RecyclerViewDecorator extends RecyclerView.ItemDecoration {
    private Paint paint;

    public RecyclerViewDecorator() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = 4;
    }
}
