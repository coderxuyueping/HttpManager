package com.halove.xyp.apilibrary;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by xyp on 2018/8/22.
 * 控制RxJava生命周期
 */

public class RxLifeCycle {

    private static RxLifeCycle instance;

    private RxLifeCycle(){}

    public static RxLifeCycle getInstance(){
        if(instance == null){
            synchronized (RxLifeCycle.class){
                if(instance == null)
                    instance = new RxLifeCycle();
            }
        }

        return instance;
    }


    //将所有正在处理的Subscription都添加到CompositeSubscription中。统一退出的时候注销观察
    private CompositeDisposable mCompositeDisposable;

    /**
     * 将Disposable添加
     *
     * @param subscription
     */
    public void addDisposable(Disposable subscription) {
        // 如果解绑了的话添加  需要新的实例否则绑定时无效的
        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(subscription);
    }

    /**
     * 在界面退出等需要解绑观察者的情况下调用此方法统一解绑，防止Rx造成的内存泄漏
     */
    public void unDisposable() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }
}
