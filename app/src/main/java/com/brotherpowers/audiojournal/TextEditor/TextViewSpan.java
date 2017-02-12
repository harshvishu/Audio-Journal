package com.brotherpowers.audiojournal.TextEditor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatEditText;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by harsh_v on 2/12/17.
 */

public class TextViewSpan extends AppCompatEditText {
    public TextViewSpan(Context context) {
        super(context);
    }

    public TextViewSpan(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewSpan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();


    @Override
    public void setText(CharSequence text, BufferType type) {
        final Spannable spannable = getTextWithImages(getContext(), text, getLineHeight(), getCurrentTextColor());
        super.setText(spannable, BufferType.SPANNABLE);
    }


    private static Spannable getTextWithImages(Context context, CharSequence text, int lineHeight, int color) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addImages(context, spannable, lineHeight, color);
        return spannable;
    }

    private static boolean addImages(Context context, Spannable spannable, int lineHeight, int color) {
        Pattern refImg = Pattern.compile("\\Q[img src=\\E([a-zA-Z0-9_]+?)\\Q/]\\E");
        boolean hasChanges = false;

        Matcher matcher = refImg.matcher(spannable);
        while (matcher.find()) {
            boolean set = true;
            for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()
                        ) {
                    spannable.removeSpan(span);
                } else {
                    set = false;
                    break;
                }
            }
            try {
                String resname = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim();
                int id = context.getResources().getIdentifier(resname, "drawable", context.getPackageName());
                if (set) {
                    hasChanges = true;
                    spannable.setSpan(makeImageSpan(context, id, lineHeight, color),
                            matcher.start(),
                            matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        return hasChanges;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        super.onTextChanged(text, start, lengthBefore, lengthAfter);
    }

    /**
     * Create an ImageSpan for the given icon drawable. This also sets the image size and colour.
     * Works best with a white, square icon because of the colouring and resizing.
     *
     * @param context       The Android Context.
     * @param drawableResId A drawable resource Id.
     * @param size          The desired size (i.e. width and height) of the image icon in pixels.
     *                      Use the lineHeight of the TextView to make the image inline with the
     *                      surrounding text.
     * @param color         The color (careful: NOT a resource Id) to apply to the image.
     * @return An ImageSpan, aligned with the bottom of the text.
     */
    private static ImageSpan makeImageSpan(Context context, int drawableResId, int size, int color) {
        final Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        drawable.setBounds(0, 0, size, size);
        return new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
    }
}
