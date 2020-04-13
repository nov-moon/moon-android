/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.meili.moon.sdk.base.widget.span;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import java.lang.ref.WeakReference;

/**
 * @author naite.zhou
 */
public class TextImageSpan extends DynamicDrawableSpan {
	private final Context mContext;
	private final int mResourceId;
	private final int mSize;
	private final int lineExtra;
	private Drawable mDrawable;

	public TextImageSpan(Context context, int resourceId, int size) {
		this(context, resourceId, size, 0);
	}
	public TextImageSpan(Context context, int resourceId, int size, int lineExtra) {
		super();
		mContext = context;
		mResourceId = resourceId;
		mSize = size;
		this.lineExtra = lineExtra;
	}

	public Drawable getDrawable() {
		if (mDrawable == null) {
			try {
				mDrawable = mContext.getResources().getDrawable(mResourceId);
				int size = mSize;
				int w;
				int h;
				if (mDrawable != null) {
					h = mDrawable.getIntrinsicHeight();
					w = mDrawable.getIntrinsicWidth();
					if (h <= 0) {
						h = size;
						w = size;
					} else {
						w = (int) ((float) size / (float) h * w);
						h = size;
					}
					mDrawable.setBounds(0, 0, w, h);
				}
			} catch (Exception e) {
			}
		}
		return mDrawable;
	}

	@Override
	public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
		Drawable b = getCachedDrawable();
		canvas.save();


		int height = bottom - top;

		int yOffset = (height - lineExtra - b.getBounds().height()) / 2;

		canvas.translate(x, yOffset + top);
		b.draw(canvas);

		canvas.restore();
	}

	private Drawable getCachedDrawable() {
		WeakReference<Drawable> wr = mDrawableRef;
		Drawable d = null;

		if (wr != null)
			d = wr.get();

		if (d == null) {
			d = getDrawable();
			mDrawableRef = new WeakReference<>(d);
		}

		return d;
	}

	private WeakReference<Drawable> mDrawableRef;
}
