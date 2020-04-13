package com.meili.moon.sdk.app.callback;


import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.common.Callback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * Created by imuto on 15/12/29.
 */
public class SimpleTypedCallback<ResultType> implements Callback.Typed, Callback.IHttpCallback<ResultType>, Callback.StartedCallback, Serializable {

    Type mResponseClass;

    public SimpleTypedCallback(Type resClass) {
        mResponseClass = resClass;
    }

    @Nullable
    @Override
    public Type getTyped() {
        return mResponseClass;
    }

    @Override
    public void setTyped(@Nullable Type type) {
        mResponseClass = type;
    }

    @Override
    public void onError(@NotNull BaseException exception) {

    }

    @Override
    public void onFinished(boolean isSuccess) {

    }

    @Override
    public void onSuccess(ResultType result) {

    }

    @Override
    public void onStarted() {

    }
}
