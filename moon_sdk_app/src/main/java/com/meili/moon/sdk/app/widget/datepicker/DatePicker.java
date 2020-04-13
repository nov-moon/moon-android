package com.meili.moon.sdk.app.widget.datepicker;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.widget.wheelview.ArrayWheelAdapter;
import com.meili.moon.sdk.app.widget.wheelview.OnWheelChangedListener;
import com.meili.moon.sdk.app.widget.wheelview.WheelView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author： fanyafeng
 * Date： 18/8/6 下午9:23
 * Email: fanyafeng@live.cn
 */
public class DatePicker extends RelativeLayout {

    private WheelView wv_year;
    private WheelView wv_month;
    private WheelView wv_day;
    private TextView mDatePickerForeverText;
    private View mDatePickerLine;

    private static final int START_YEAR = 1901;
    private static final int END_YEAR = 2099;
    private int mStartYear = START_YEAR;
    private int mEndYear = END_YEAR;
    private boolean mSolar = true;
    private boolean mYearShow = true;
    private boolean mLeapMonthShow = true;

    private boolean isStillNow = false;

    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private Long currentTime;
    private boolean canForever;

    public DatePicker(Context context) {
        super(context);
        init(context);
    }

    public DatePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DatePicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.date_picker_layout, this);

        wv_year = findViewById(R.id.year);
        wv_year.setVisibleItems(5);
        wv_month = findViewById(R.id.month);
        wv_month.setVisibleItems(5);
        wv_day = findViewById(R.id.day);
        wv_day.setVisibleItems(5);
        mDatePickerForeverText = findViewById(R.id.mDatePickerForeverText);
        mDatePickerLine = findViewById(R.id.mDatePickerLine);
        mDatePickerForeverText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setForever(true);
            }
        });

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        currentTime = date.getTime();
        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    /**
     * 谁知Datepicker是否显示长期标签
     * @param canForever
     */
    public void setCanForever(boolean canForever) {
        mDatePickerForeverText.setVisibility(canForever ? View.VISIBLE : View.INVISIBLE);
        mDatePickerLine.setVisibility(canForever ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 设置长期标签的显示状态
     * @param forever
     */
    public void setForever(boolean forever) {
        setForeverInternal(forever,false);
    }

    private void setForeverInternal(boolean forever, boolean isInternal) {
        if (forever) {
            setCanForever(true);
            mDatePickerForeverText.setTextColor(getResources().getColor(R.color.black));
            mDatePickerForeverText.setBackgroundResource(R.drawable.date_pick_forever_bg);
        } else {
            mDatePickerForeverText.setTextColor(Color.parseColor("#999999"));
            mDatePickerForeverText.setBackgroundResource(R.drawable.date_pick_forever_white_bg);
        }
        mDatePickerForeverText.setSelected(forever);
        if (mOnDateTimeSetListener != null && !isInternal) {
            mOnDateTimeSetListener.onDateTimeSet(this);
        }
    }

    public boolean isForever() {
        return mDatePickerForeverText.isSelected();
    }

    public boolean isCanForever() {
        int visibility = mDatePickerForeverText.getVisibility();
        return visibility == View.VISIBLE;
    }

    private OnDateTimeSetListener mOnDateTimeSetListener;

    public void setOnDateTimeSetListener(OnDateTimeSetListener l) {
        this.mOnDateTimeSetListener = l;
    }

    public interface OnDateTimeSetListener {
        void onDateTimeSet(DatePicker picker);
    }

    private OnAbortPickerListener mOnAbortListener;

    public void setEndDate(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
//        int year = calendar.get(Calendar.YEAR);
//        END_YEAR = year;
//        invalidate();
        setEndDate(calendar);
    }

    public void setEndDate(Calendar cal) {
        mEndYear = cal.get(Calendar.YEAR);
        initDateTimePicker(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    }

    public void setMinDate(Long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        mStartYear = calendar.get(Calendar.YEAR);
        wv_year.setAdapter(new NumericWheelAdapter(mStartYear, mEndYear));
        refreshSelected();
    }

    public void setDate(Long time) {
        currentTime = time;
        refreshSelected();
    }

    /**
     * 刷新当前选中的年月日。
     */
    private void refreshSelected() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTime));
//        initDateTimePicker(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        wv_year.setCurrentItem(calendar.get(Calendar.YEAR) - mStartYear);
        wv_month.setCurrentItem(calendar.get(Calendar.MONTH));
        wv_day.setCurrentItem(calendar.get(Calendar.DAY_OF_MONTH) - 1);
    }


    public interface OnAbortPickerListener {
        void OnAbort(DatePicker picker);
    }

    public boolean isTillNow() {
        return isStillNow;
    }

    public void setTillNow(boolean stillNow) {
        isStillNow = stillNow;
        Calendar cal = Calendar.getInstance();
        initDateTimePicker(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        mEndYear = cal.get(Calendar.YEAR);
        wv_year.setAdapter(new NumericWheelAdapter(mStartYear, cal.get(Calendar.YEAR)));
    }

    public void initDateTimePicker(int year, int month, int day) {
        currentYear = year;
        currentMonth = month + 1;
        currentDay = day;
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        LunarItem lunarItem = new LunarItem(cal);

        // 添加大小月月份并将其转换为list,方便之后的判断
        String[] months_big = {"1", "3", "5", "7", "8", "10", "12"};
        String[] months_little = {"4", "6", "9", "11"};

        final List<String> list_big = Arrays.asList(months_big);
        final List<String> list_little = Arrays.asList(months_little);

        // 年

        if (!mYearShow) {
            wv_year.setVisibility(View.GONE);
        } else {
            wv_year.setVisibility(View.VISIBLE);
        }
        wv_year.setAdapter(new NumericWheelAdapter(mStartYear, mEndYear));// 设置"年"的显示数据
        wv_year.setCyclic(false);// 可循环滚动
//        wv_year.setLabel("年");// 添加文字
        if (mSolar) {
            wv_year.setCurrentItem(year - mStartYear);// 初始化时显示的数据
        } else {
            wv_year.setCurrentItem(lunarItem.getYear() - mStartYear);
        }

        wv_year.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                // TODO: 18/8/7
                setForeverInternal(false,true);
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.onDateTimeSet(DatePicker.this);
                }
            }
        });

        // 月

        if (mSolar) {
            if (isStillNow && currentYear == getYear()) {
                wv_month.setAdapter(new NumericWheelAdapter(1, currentMonth));
                wv_month.setCurrentItem(month);
            } else {
                wv_month.setAdapter(new NumericWheelAdapter(1, 12));
                wv_month.setCurrentItem(month);
            }
        } else {
            wv_month.setAdapter(new ArrayWheelAdapter<String>(getLunarMonthArray(lunarItem.getYear())));
            int lunarMonth = lunarItem.getMonth() + 1;
            if (mLeapMonthShow) {
                if ((lunarMonth > LunarItem.leapMonth(lunarItem.getYear()) && LunarItem.leapMonth(lunarItem.getYear()) > 0) || lunarItem.isLeep()) {
                    lunarMonth++;
                }
            }
            wv_month.setCurrentItem(lunarMonth - 1);
        }
        wv_month.setCyclic(false);
//        wv_month.setLabel("月");
        wv_month.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                setForeverInternal(false,true);
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.onDateTimeSet(DatePicker.this);
                }
            }
        });

        // 日

        wv_day.setCyclic(false);
        // 判断大小月及是否闰年,用来确定"日"的数据
        if (mSolar) {
            if (isStillNow && currentYear == getYear() && getMonth() == (currentMonth - 1)) {
                wv_day.setAdapter(new NumericWheelAdapter(1, currentDay));
            } else {
                if (list_big.contains(String.valueOf(month + 1))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 31));
                } else if (list_little.contains(String.valueOf(month + 1))) {
                    wv_day.setAdapter(new NumericWheelAdapter(1, 30));
                } else {
                    // 闰年
                    if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
                        wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                    else
                        wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                }
            }
            wv_day.setCurrentItem(day - 1);
        } else {
            wv_day.setAdapter(new ArrayWheelAdapter<String>(getLunarDayArray(lunarItem.getYear(), lunarItem.getMonth() + 1)));
            wv_day.setCurrentItem(lunarItem.getDay() - 1);
        }
        wv_day.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                setForeverInternal(false,true);
                if (mOnDateTimeSetListener != null) {
                    mOnDateTimeSetListener.onDateTimeSet(DatePicker.this);
                }
            }
        });

        updateDayLabel();

        // 添加"年"监听
        OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int year_num = newValue + mStartYear;
                if (mSolar) {
                    if (isStillNow && currentYear == getYear()) {
                        wv_month.setAdapter(new NumericWheelAdapter(1, currentMonth));
                        wv_day.setAdapter(new NumericWheelAdapter(1, currentDay));
                    } else {
                        wv_month.setAdapter(new NumericWheelAdapter(1, 12));
                        if (list_big.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
                            wv_day.setAdapter(new NumericWheelAdapter(1, 31));
                        } else if (list_little.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
                            wv_day.setAdapter(new NumericWheelAdapter(1, 30));
                        } else {
                            if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
                                wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                            else
                                wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                        }
                    }
                } else {
                    wv_month.setAdapter(new ArrayWheelAdapter<String>(getLunarMonthArray(year_num)));
                    wv_day.setAdapter(new ArrayWheelAdapter<String>(getLunarDayArray(year_num, wv_month.getCurrentItem() + 1)));
                }

                if (wv_month.getCurrentItem() >= wv_month.getAdapter().getItemsCount()) {
                    wv_month.setCurrentItem(wv_month.getAdapter().getItemsCount() - 1, true);
                }

                if (wv_day.getCurrentItem() >= wv_day.getAdapter().getItemsCount()) {
                    wv_day.setCurrentItem(wv_day.getAdapter().getItemsCount() - 1, true);
                }
                updateDayLabel();
            }
        };
        wv_year.addChangingListener(wheelListener_year);

        // 添加"月"监听
        OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                int month_num = newValue + 1;
                if (mSolar) {
                    if (isStillNow && currentYear == getYear() && getMonth() == (currentMonth - 1)) {
                        wv_day.setAdapter(new NumericWheelAdapter(1, currentDay));
                    } else {
                        if (list_big.contains(String.valueOf(month_num))) {
                            wv_day.setAdapter(new NumericWheelAdapter(1, 31));
                        } else if (list_little.contains(String.valueOf(month_num))) {
                            wv_day.setAdapter(new NumericWheelAdapter(1, 30));
                        } else {
                            if (((wv_year.getCurrentItem() + mStartYear) % 4 == 0 && (wv_year.getCurrentItem() + mStartYear) % 100 != 0) || (wv_year.getCurrentItem() + mStartYear) % 400 == 0)
                                wv_day.setAdapter(new NumericWheelAdapter(1, 29));
                            else
                                wv_day.setAdapter(new NumericWheelAdapter(1, 28));
                        }
                    }
                } else {
                    wv_day.setAdapter(new ArrayWheelAdapter<String>(getLunarDayArray(wv_year.getCurrentItem() + mStartYear, month_num)));
                }

                if (wv_day.getCurrentItem() >= wv_day.getAdapter().getItemsCount()) {
                    wv_day.setCurrentItem(wv_day.getAdapter().getItemsCount() - 1, true);
                }

                updateDayLabel();
            }
        };
        wv_month.addChangingListener(wheelListener_month);

        // 添加"日"监听
        OnWheelChangedListener wheelListener_day = new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                updateDayLabel();
            }
        };
        wv_day.addChangingListener(wheelListener_day);

    }

    private void updateDayLabel() {
        int year = getYear();
        int month = getMonth();
        int day = getDay();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

//        wv_day.setLabel(DateFormatManager.DayOfWeekDisplay(calendar.get(Calendar.DAY_OF_WEEK)));
    }

    private String[] getLunarMonthArray(int year) {
        List<String> list = new ArrayList<String>();

        int leapMonth = LunarItem.leapMonth(year);
        if (!mYearShow) {
            leapMonth = 0;
        }

        for (int i = 1; i <= 12; i++) {
            list.add(LunarItem.getChinaMonthString(i, false).replaceAll("月", ""));
            if (mLeapMonthShow && i == leapMonth) {
                list.add(LunarItem.getChinaMonthString(i, true).replaceAll("月", ""));
            }
        }
        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }

    private String[] getLunarMonthArrayIsStillNow(int year, int month) {
        List<String> list = new ArrayList<String>();

        int leapMonth = LunarItem.leapMonth(year);
        if (!mYearShow) {
            leapMonth = 0;
        }

        int count = 1;
        String lunarMonth = LunarItem.getChinaMonthString(month, false);
        if (lunarMonth.contains("正")) {
            count = 1;
        } else if (lunarMonth.contains("二")) {
            count = 2;
        } else if (lunarMonth.contains("三")) {
            count = 3;
        } else if (lunarMonth.contains("四")) {
            count = 4;
        } else if (lunarMonth.contains("五")) {
            count = 5;
        } else if (lunarMonth.contains("六")) {
            count = 6;
        } else if (lunarMonth.contains("七")) {
            count = 7;
        } else if (lunarMonth.contains("八")) {
            count = 8;
        } else if (lunarMonth.contains("九")) {
            count = 9;
        } else if (lunarMonth.contains("十")) {
            count = 10;
        } else if (lunarMonth.contains("冬")) {
            count = 11;
        } else if (lunarMonth.contains("腊")) {
            count = 12;
        }

        for (int i = 1; i <= count; i++) {
            list.add(LunarItem.getChinaMonthString(i, false).replaceAll("月", ""));
            if (mLeapMonthShow && i == leapMonth) {
                list.add(LunarItem.getChinaMonthString(i, true).replaceAll("月", ""));
            }
        }
        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }

    private String[] getLunarDayArray(int year, int month) {
        List<String> list = new ArrayList<String>();
        for (int i = 1; i <= LunarItem.getMonthDays(year, month); i++) {
            list.add(LunarItem.getChinaDayString(i));
        }
        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }

    private String[] getLunarDayArrayIsStillNow() {
        List<String> list = new ArrayList<String>();

        Calendar cal = Calendar.getInstance();
        LunarItem lunarItem = new LunarItem(cal);

        for (int i = 1; i <= lunarItem.getDay(); i++) {
            list.add(LunarItem.getChinaDayString(i));
        }
        // TODO: 17/9/18
        String[] result = new String[list.size()];
        list.toArray(result);
        return result;
    }

    public int getYear() {
        if (mSolar) {
            return wv_year.getCurrentItem() + mStartYear;
        } else {
            int month = wv_month.getCurrentItem() + 1;
            if (mLeapMonthShow) {
                int leapMonth = LunarItem.leapMonth(wv_year.getCurrentItem() + mStartYear);
                if (leapMonth > 0 && month > leapMonth) {
                    month--;
                    if (month == leapMonth) {
                        month += 12;
                    }
                }
            }
            return LunarParser.parseToSolar(wv_year.getCurrentItem() + mStartYear, month, wv_day.getCurrentItem() + 1)[0];
        }
    }

    /**
     * @return month in [0..11]
     */
    public int getMonth() {
        if (mSolar) {
            return wv_month.getCurrentItem();
        } else {
            int month = wv_month.getCurrentItem() + 1;
            if (mLeapMonthShow) {
                int leapMonth = LunarItem.leapMonth(wv_year.getCurrentItem() + mStartYear);
                if (leapMonth > 0 && month > leapMonth) {
                    month--;
                    if (month == leapMonth) {
                        month += 12;
                    }
                }
            }
            return LunarParser.parseToSolar(wv_year.getCurrentItem() + mStartYear, month, wv_day.getCurrentItem() + 1)[1] - 1;
        }
    }

    public int getRawMonth() {
        return wv_month.getCurrentItem();
    }

    public int getDay() {
        if (mSolar) {
            return wv_day.getCurrentItem() + 1;
        } else {
            int month = wv_month.getCurrentItem() + 1;
            if (mLeapMonthShow) {
                int leapMonth = LunarItem.leapMonth(wv_year.getCurrentItem() + mStartYear);
                if (leapMonth > 0 && month > leapMonth) {
                    month--;
                    if (month == leapMonth) {
                        month += 12;
                    }
                }
            }
            return LunarParser.parseToSolar(wv_year.getCurrentItem() + mStartYear, month, wv_day.getCurrentItem() + 1)[2];
        }
    }

    public int getRawDay() {
        return wv_day.getCurrentItem() + 1;
    }


    public Calendar getTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getYear(), getMonth(), getDay(), 0, 0);
        return calendar;
    }

    public long getLongTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getYear(), getMonth(), getDay(), 0, 0);
        return calendar.getTimeInMillis();
    }

    public boolean isSolar() {
        return mSolar;
    }

    public boolean isIgnoreYear() {
        return !mYearShow;
    }
}
