package com.brotherpowers.audiojournal.View;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by harsh_v on 2/12/17.
 */

public class AJViewPager extends ViewPager {
    public AJViewPager(Context context) {
        super(context);
    }

    public AJViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Enable disable scrolling of pages
     */
    public boolean isPagingEnabled = true;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isPagingEnabled && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptHoverEvent(MotionEvent event) {
        return isPagingEnabled && super.onInterceptHoverEvent(event);
    }
}
