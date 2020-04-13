package com.meili.moon.sdk.app.widget.picker;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.base.util.DensityUtilsKt;

import java.util.Calendar;
import java.util.Date;

public class DatePicker extends LinearLayout implements NumberPicker.OnValueChangeListener {

    private NumberPicker mYearPicker;
    private NumberPicker mMonthPicker;
    private NumberPicker mDayOfMonthPicker;

    private Calendar mCalendar;

    private OnDateChangedListener mOnDateChangedListener;

    private LayoutInflater mLayoutInflater;

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        init();
    }

    private void init() {
        mLayoutInflater.inflate(R.layout.moon_sdk_app_widget_date_picker, this, true);
        mYearPicker = (NumberPicker) findViewById(R.id.date_picker_year);
        mMonthPicker = (NumberPicker) findViewById(R.id.date_picker_month);
        mDayOfMonthPicker = (NumberPicker) findViewById(R.id.date_picker_day);
        mMonthPicker.setStartNumber(1);
        mMonthPicker.setEndNumber(12);
        mDayOfMonthPicker.setStartNumber(1);
        mDayOfMonthPicker.setEndNumber(31);

        mYearPicker.setOnValueChangeListener(this);
        mMonthPicker.setOnValueChangeListener(this);
        mDayOfMonthPicker.setOnValueChangeListener(this);

        if (!getResources().getConfiguration().locale.getCountry().equals("CN")
                && !getResources().getConfiguration().locale.getCountry().equals("TW")) {

            String[] monthNames = getResources().getStringArray(R.array.moon_sdk_app_widget_data_picker_month_name);
            mMonthPicker.setCustomTextArray(monthNames);
        }

        mCalendar = Calendar.getInstance();
        setDate(mCalendar.getTime());
        setMaxDate(mCalendar.getTimeInMillis());

        int dp = DensityUtilsKt.getDp(20);
        setPadding(dp, getPaddingTop(), dp, getPaddingBottom());
    }

    public DatePicker setDate(Long time) {
        return setDate(new Date(time));
    }

    public DatePicker setDate(Date date) {
        mCalendar.setTime(date);
        mDayOfMonthPicker.setEndNumber(mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        mYearPicker.setCurrentNumber(mCalendar.get(Calendar.YEAR));
        mMonthPicker.setCurrentNumber(mCalendar.get(Calendar.MONTH) + 1);
        mDayOfMonthPicker.setCurrentNumber(mCalendar.get(Calendar.DAY_OF_MONTH));

        return this;
    }

    public void setMaxDate(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        int year = calendar.get(Calendar.YEAR);
        setMaxYear(year);
    }

    public void setMinDate(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        int year = calendar.get(Calendar.YEAR);
        mYearPicker.setStartNumber(year);
    }

    public void setShowDayPicker(boolean isShow) {
        mDayOfMonthPicker.setVisibility(isShow ? VISIBLE : GONE);
    }

    public void addMaxYear(int years) {
        setMaxYear(mCalendar.get(Calendar.YEAR) + years);
    }

    public void setFromNow(boolean fromNow) {
        if (fromNow) {
            mYearPicker.setStartNumber(mCalendar.get(Calendar.YEAR));
        } else {
            mYearPicker.setStartNumber(1990);
        }
    }

    public void setMaxYear(int year) {
        if (year <= 0) {
            return;
        }
        mYearPicker.setEndNumber(year);
    }

    public void setUseDateOfMonth(boolean isUse) {
        mDayOfMonthPicker.setVisibility(isUse ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onValueChange(final NumberPicker picker, final int oldVal, final int newVal) {

        if (picker == mYearPicker) {
            int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.set(newVal, mCalendar.get(Calendar.MONTH), 1);
            int lastDayOfMonth = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (dayOfMonth > lastDayOfMonth) {
                dayOfMonth = lastDayOfMonth;
            }
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mDayOfMonthPicker.setEndNumber(lastDayOfMonth);
        } else if (picker == mMonthPicker) {
            int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.set(mCalendar.get(Calendar.YEAR), newVal - 1, 1);
            int lastDayOfMonth = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (dayOfMonth > lastDayOfMonth) {
                dayOfMonth = lastDayOfMonth;
            }
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            mDayOfMonthPicker.setEndNumber(lastDayOfMonth);
        } else if (picker == mDayOfMonthPicker) {
            mCalendar.set(Calendar.DAY_OF_MONTH, newVal);
        }

        notifyDateChanged();
    }

    /**
     * The callback used to indicate the user changes\d the date.
     */
    public interface OnDateChangedListener {

        /**
         * Called upon a date change.
         *
         * @param view        The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *                    with {@link Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    public DatePicker setOnDateChangedListener(OnDateChangedListener l) {
        mOnDateChangedListener = l;
        return this;
    }

    private void notifyDateChanged() {
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }
    }

    public int getYear() {
        return mCalendar.get(Calendar.YEAR);
    }

    public int getMonth() {
        return mCalendar.get(Calendar.MONTH) + 1;
    }

    public int getDayOfMonth() {
        return mCalendar.get(Calendar.DAY_OF_MONTH);
    }

    public long getTime() {
        return mCalendar.getTimeInMillis();
    }

    public DatePicker setSoundEffect(Sound sound) {
        mYearPicker.setSoundEffect(sound);
        mMonthPicker.setSoundEffect(sound);
        mDayOfMonthPicker.setSoundEffect(sound);
        return this;
    }

    @Override
    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        super.setSoundEffectsEnabled(soundEffectsEnabled);
        mYearPicker.setSoundEffectsEnabled(soundEffectsEnabled);
        mMonthPicker.setSoundEffectsEnabled(soundEffectsEnabled);
        mDayOfMonthPicker.setSoundEffectsEnabled(soundEffectsEnabled);
    }

    public DatePicker setRowNumber(int rowNumber) {
        mYearPicker.setRowNumber(rowNumber);
        mMonthPicker.setRowNumber(rowNumber);
        mDayOfMonthPicker.setRowNumber(rowNumber);
        return this;
    }

    public DatePicker setTextSize(float textSize) {
        mYearPicker.setTextSize(textSize);
        mMonthPicker.setTextSize(textSize);
        mDayOfMonthPicker.setTextSize(textSize);
        return this;
    }

    public DatePicker setFlagTextSize(float textSize) {
        mYearPicker.setFlagTextSize(textSize);
        mMonthPicker.setFlagTextSize(textSize);
        mDayOfMonthPicker.setFlagTextSize(textSize);
        return this;
    }

    public DatePicker setTextColor(int color) {
        mYearPicker.setTextColor(color);
        mMonthPicker.setTextColor(color);
        mDayOfMonthPicker.setTextColor(color);
        return this;
    }

    public DatePicker setFlagTextColor(int color) {
        mYearPicker.setFlagTextColor(color);
        mMonthPicker.setFlagTextColor(color);
        mDayOfMonthPicker.setFlagTextColor(color);
        return this;
    }

    public DatePicker setBackground(int color) {
        super.setBackgroundColor(color);
        mYearPicker.setBackground(color);
        mMonthPicker.setBackground(color);
        mDayOfMonthPicker.setBackground(color);
        return this;
    }

}
