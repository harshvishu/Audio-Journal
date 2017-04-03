package com.brotherpowers.audiojournal.View;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by harsh_v on 3/29/17.
 */

public class ProgressButton extends AppCompatImageButton {
    public ProgressButton(Context context) {
        super(context, null, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        configure();
    }

    private void configure(){
        GestureDetectorCompat gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                System.out.println(">>>>> onDown");
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {
                System.out.println(">>>>> onShowPress");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                System.out.println(">>>>> onSingleTapUp");
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                System.out.println(">>>>> onScroll");
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                System.out.println(">>>>> onLongPress");
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

    }
}
