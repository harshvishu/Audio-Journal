package com.brotherpowers.audiojournal.View;

import android.content.Context;
import android.os.Build;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.ViewSwitcher;

import com.brotherpowers.audiojournal.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.Date;

import butterknife.ButterKnife;

/**
 * Created by harsh_v on 3/20/17.
 */

public class DateTimePicker extends RelativeLayout implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener {
    public static final int PICKER_DATE_PICKER = 0;
    public static final int PICKER_TIME_PICKER = 1;


    @IntDef({PICKER_DATE_PICKER, PICKER_TIME_PICKER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Picker {
    }

    private final ViewSwitcher _viewSwitcher;
    private final DatePicker _datePicker;
    private final TimePicker _timePicker;
    private int picker;

    private Calendar calendar = Calendar.getInstance();

    public DateTimePicker(Context context) {
        this(context, null);
    }

    public DateTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final LayoutInflater inflater = LayoutInflater.from(context);

        _viewSwitcher = new ViewSwitcher(context);
        _viewSwitcher.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // Set animation
        _viewSwitcher.setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
        _viewSwitcher.setOutAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right));


        // Inflate the date and time picker views
//        final LinearLayout datePickerView = (LinearLayout) inflater.inflate(R.layout.date_picker, this, false);
        _datePicker = (DatePicker) inflater.inflate(R.layout.date_picker, this, false);


//        final LinearLayout timePickerView = (LinearLayout) inflater.inflate(R.layout.time_picker, this, false);
        _timePicker = (TimePicker) inflater.inflate(R.layout.time_picker, this, false);


        _viewSwitcher.addView(_datePicker, 0);
        _viewSwitcher.addView(_timePicker, 1);

        addView(_viewSwitcher, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Set current picker
        picker = PICKER_DATE_PICKER;

        // Refresh the data
        refresh();
    }

    public void setDate(Date date) {
        this.calendar.setTime(date);
        refresh();
    }


    public Date getTime() {
        return calendar.getTime();
    }

    private void refresh() {
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        _datePicker.init(year, month, day, this);
        _timePicker.setOnTimeChangedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            _timePicker.setHour(hour);
        } else {
            _timePicker.setCurrentHour(hour);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            _timePicker.setMinute(minute);
        } else {
            _timePicker.setCurrentMinute(minute);
        }

    }

    public void setMinDate(long timeMillis) {
        _datePicker.setMinDate(timeMillis);
    }

    public void showTimePicker() {
        if (_viewSwitcher.getCurrentView() instanceof TimePicker) {
            return;
        }
        _viewSwitcher.showNext();
        picker = PICKER_TIME_PICKER;
    }

    public void showDatePicker() {
        if (_viewSwitcher.getCurrentView() instanceof DatePicker) {
            return;
        }
        _viewSwitcher.showNext();
        picker = PICKER_DATE_PICKER;
    }

    @Picker
    public int getPicker() {
        return picker;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
        calendar.set(Calendar.MINUTE,minute);
    }

}
