package com.halove.xyp.apilibrary.observer;

import android.text.TextUtils;
import android.util.Log;

import com.halove.xyp.apilibrary.ApiConstant;
import com.halove.xyp.apilibrary.BaseResponse;
import com.halove.xyp.apilibrary.RxLifeCycle;
import com.halove.xyp.apilibrary.utils.GsonUtil;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * Created by xyp on 2018/3/14.
 * 同時处理接口返回的jsonObject与jsonArray
 */

public abstract class SimpleObserver<T> implements Observer<ResponseBody> {
    Class<T> tClass;
    Class<T[]> tClassList;

    //可以处理两种情况：解析jsonObject和解析jsonArray,两个参数必须要有一个为null
    protected SimpleObserver(Class<T> tClass, Class<T[]> tClassList) {
        this.tClass = tClass;
        this.tClassList = tClassList;
    }

    @Override
    public void onSubscribe(Disposable d) {
        RxLifeCycle.getInstance().addDisposable(d);
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        String res = "";
        try {
            res = responseBody.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(TextUtils.isEmpty(res)){
            onError("请求失败");
            return;
        }

        parseJson(res);
    }

    /**
     * 默认使用基础BaseResponse去解析外层json，如果有其他需求，可以重写该方法
     * @param res
     */
    protected void parseJson(String res){
        BaseResponse baseResponse = GsonUtil.getObject(res, BaseResponse.class);
        //如果code跟服务器定下的成功返回不一致
        if(!ApiConstant.CODE_SUCCESS.equals(baseResponse.getCode())){
            onError(baseResponse.getMsg());
            return;
        }
        if (tClass != null) {
            T t = GsonUtil.getObject(baseResponse.getData(), tClass);
            onDataSuccess(t);
            return;
        }

        if(tClassList != null){
            List<T> list = GsonUtil.getObjects(baseResponse.getData(), tClassList);
            onDataSuccess(list);
        }
    }



    /**
     * 內部释放了订阅，但是在四大组件的生命周期最好手动释放，防止请求未结束的情况
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        RxLifeCycle.getInstance().unDisposable();
        if(e instanceof UnknownHostException){
            onError("网络错误");
        }else{
            onError(e.getMessage());
            Log.d("xudaha", e.toString());
        }
    }

    @Override
    public void onComplete() {
        RxLifeCycle.getInstance().unDisposable();
    }




    public void onDataSuccess(T t) {
    }

    public void onDataSuccess(List<T> t) {
    }

    public abstract void onError(String msg);

//    public abstract void addDisposable(Disposable d);
}
