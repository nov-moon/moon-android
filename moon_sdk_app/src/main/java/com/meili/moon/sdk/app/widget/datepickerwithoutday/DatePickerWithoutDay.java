package com.meili.moon.sdk.app.widget.datepickerwithoutday;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.widget.wheelview.OnWheelChangedListener;
import com.meili.moon.sdk.app.widget.wheelview.WheelView;

import java.util.Calendar;
import java.util.Date;

/**
 * Author： fanyafeng
 * Date： 2018/12/11 11:18 AM
 * Email: fanyafeng@live.cn
 */
public class DatePickerWithoutDay extends RelativeLayout {
    private WheelView wvYear;
    private WheelView wvMonth;

    private static final int START_YEAR = 1901;
    private static final int END_YEAR = 2099;
    private int mStartYear = START_YEAR;
    private int mStartMonth = 1;
    private int mEndYear = END_YEAR;
    private int mEndMonth = 12;

    private OnDateTimeSetListener mOnDateTimeSetListener;

    public void setOnDateTimeSetListener(OnDateTimeSetListener l) {
        this.mOnDateTimeSetListener = l;
    }

    public interface OnDateTimeSetListener {
        void onDateTimeSet(DatePickerWithoutDay picker);
    }

    public DatePickerWithoutDay(Context context) {
        super(context);
        init(context);
    }

    public DatePickerWithoutDay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DatePickerWithoutDay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setStartData(Calendar calendar) {
        mStartYear = calendar.get(Calendar.YEAR);
        mStartMonth = calendar.get(Calendar.MONTH) + 1;
        wvYear.setAdapter(new NumericWheelLabelAdapter(calendar.get(Calendar.YEAR), mEndYear, "年"));
        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    public void setStartData(Long startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(startTime));
        mStartYear = calendar.get(Calendar.YEAR);
        mStartMonth = calendar.get(Calendar.MONTH) + 1;
        wvYear.setAdapter(new NumericWheelLabelAdapter(calendar.get(Calendar.YEAR), mEndYear, "年"));
        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    public void setEndData(Calendar calendar) {
        mEndYear = calendar.get(Calendar.YEAR);
        mEndMonth = calendar.get(Calendar.MONTH) + 1;
        wvYear.setAdapter(new NumericWheelLabelAdapter(mStartYear, calendar.get(Calendar.YEAR), "年"));
        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    public void setEndData(Long endTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(endTime));
        mEndYear = calendar.get(Calendar.YEAR);
        mEndMonth = calendar.get(Calendar.MONTH) + 1;
        wvYear.setAdapter(new NumericWheelLabelAdapter(mStartYear, calendar.get(Calendar.YEAR), "年"));
        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    public void setDate(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    public void setDate(Calendar calendar) {
        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.date_picker_without_day_layout, this);

        wvYear = findViewById(R.id.year);
        wvYear.setVisibleItems(5);
        wvMonth = findViewById(R.id.month);
        wvMonth.setVisibleItems(5);

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    public void initDateTimePicker(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);

        // 年
        wvYear.setAdapter(new NumericWheelLabelAdapter(mStartYear, mEndYear, "年"));// 设置"年"的显示数据
        wvYear.setCyclic(false);// 可循环滚动

        wvYear.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.onDateTimeSet(DatePickerWithoutDay.this);
                }
            }
        });

        // 月
        if (getYear() == mStartYear) {
            wvMonth.setAdapter(new NumericWheelLabelAdapter(mStartMonth, 12, "月"));
        } else if (getYear() == mEndYear) {
            wvMonth.setAdapter(new NumericWheelLabelAdapter(1, mEndMonth, "月"));
        } else {
            wvMonth.setAdapter(new NumericWheelLabelAdapter(1, 12, "月"));
        }
        wvMonth.setCyclic(false);

        wvMonth.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.onDateTimeSet(DatePickerWithoutDay.this);
                }
            }
        });


        // 添加"年"监听
        OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (getYear() == mStartYear) {
                    wvMonth.setAdapter(new NumericWheelLabelAdapter(mStartMonth, 12, "月"));
                } else if (getYear() == mEndYear) {
                    wvMonth.setAdapter(new NumericWheelLabelAdapter(1, mEndMonth, "月"));
                } else {
                    wvMonth.setAdapter(new NumericWheelLabelAdapter(1, 12, "月"));
                }

                if (wvMonth.getCurrentItem() >= wvMonth.getAdapter().getItemsCount()) {
                    wvMonth.setCurrentItem(wvMonth.getAdapter().getItemsCount() - 1, true);
                }

                updateDayLabel();
            }
        };
        wvYear.addChangingListener(wheelListener_year);

        wvYear.setCurrentItem(year - mStartYear);// 初始化时显示的数据

        if (getYear() == mStartYear) {
            wvMonth.setCurrentItem(month - mStartMonth + 1);
        } else {
            wvMonth.setCurrentItem(month);
        }

        // 添加"月"监听
        OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                updateDayLabel();
            }
        };
        wvMonth.addChangingListener(wheelListener_month);

    }

    public int getYear() {
        return wvYear.getCurrentItem() + mStartYear;
    }

    /**
     * @return month in [0..11]
     */
    public int getMonth() {
        if ((wvYear.getCurrentItem() + mStartYear) == mStartYear) {
            return wvMonth.getCurrentItem() + mStartMonth - 1;
        } else {
            return wvMonth.getCurrentItem();
        }
    }

    public long getLongTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getYear(), getMonth(), 1, 0, 0);
        return calendar.getTimeInMillis();
    }

    private void updateDayLabel() {
        int year = getYear();
        int month = getMonth();
        int day = 1;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
    }
}
