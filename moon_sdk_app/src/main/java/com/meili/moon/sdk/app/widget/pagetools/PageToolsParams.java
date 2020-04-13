package com.meili.moon.sdk.app.widget.pagetools;

import android.database.Observable;
import android.view.View;

/**
 * Created by imuto on 15/12/14.
 */
public final class PageToolsParams {

    int background;
    CharSequence title;
    CharSequence content;
    Object tag;
    int drawable;

    CharSequence leftBtnText;
    View.OnClickListener leftBtnListener;
    CharSequence rightBtnText;
    View.OnClickListener rightBtnListener;

    boolean clickable;

    private PageToolsObservable mPageToolsObservable = new PageToolsObservable();

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    public void setImage(int drawable) {
        this.drawable = drawable;
    }

    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public PageToolsParams setContent(CharSequence content) {
        this.content = content;
        return this;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public void setLeftButton(CharSequence text, View.OnClickListener listener) {
        this.leftBtnText = text;
        this.leftBtnListener = listener;
    }

    public void setRightButton(CharSequence text, View.OnClickListener listener) {
        this.rightBtnText = text;
        this.rightBtnListener = listener;
    }


    //--- getter

    public boolean isClickable() {
        return clickable;
    }

    public int getBackground() {
        return background;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getContent() {
        return content;
    }

    public int getDrawable() {
        return drawable;
    }

    public CharSequence getLeftBtnText() {
        return leftBtnText;
    }

    public View.OnClickListener getLeftBtnListener() {
        return leftBtnListener;
    }

    public CharSequence getRightBtnText() {
        return rightBtnText;
    }

    public View.OnClickListener getRightBtnListener() {
        return rightBtnListener;
    }

    public void notifyDataSetChanged() {
        mPageToolsObservable.notifyChanged();
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mPageToolsObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mPageToolsObservable.unregisterObserver(observer);
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public static class PageToolsObservable extends Observable<DataSetObserver> {
        public void notifyChanged() {
            synchronized (mObservers) {
                for (int i = mObservers.size() - 1; i >= 0; i--) {
                    mObservers.get(i).onChanged();
                }
            }
        }
    }

    public static abstract class DataSetObserver {
        public abstract void onChanged();
    }
}
