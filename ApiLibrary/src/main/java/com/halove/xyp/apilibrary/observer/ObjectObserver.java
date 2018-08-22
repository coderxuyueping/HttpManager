package com.halove.xyp.apilibrary.observer;

/**
 * Created by xyp on 2018/8/22.
 * 返回接口中的jsonObject
 */

public abstract class ObjectObserver<T> extends SimpleObserver<T> {

    public ObjectObserver(Class<T> tClass) {
        super(tClass, null);
    }


    @Override
    public void onDataSuccess(T t) {
        onSuccess(t);
    }


    public abstract void onSuccess(T t);
}
