package com.halove.xyp.apilibrary.upload;

import android.text.TextUtils;
import android.util.Log;

import com.halove.xyp.apilibrary.ApiConstant;
import com.halove.xyp.apilibrary.BaseResponse;
import com.halove.xyp.apilibrary.RxLifeCycle;
import com.halove.xyp.apilibrary.utils.GsonUtil;

import java.io.IOException;
import java.net.UnknownHostException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * Created by xyp on 2018/3/14.
 * 带进度条的上传下载监听
 */

public abstract class ProgressObserver implements Observer<ResponseBody> {
    private boolean download;

    public ProgressObserver(){}

    public ProgressObserver(boolean download){
        this.download = download;
    }

    @Override
    public void onSubscribe(Disposable d) {
        RxLifeCycle.getInstance().addDisposable(d);
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        if(download){
            onSuccess();
            return;
        }
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

        BaseResponse baseResponse = GsonUtil.getObject(res, BaseResponse.class);
        //如果code跟服务器定下的成功返回不一致
        if(!ApiConstant.CODE_SUCCESS.equals(baseResponse.getCode())){
            onError(baseResponse.getMsg());
            return;
        }

        onSuccess();
    }

    @Override
    public void onError(Throwable e) {
        RxLifeCycle.getInstance().unDisposable();
        if(e instanceof UnknownHostException){
            onError("网络错误");
        }else{
            onError("");
            Log.d("xudaha", e.toString());
        }
    }

    @Override
    public void onComplete() {
        RxLifeCycle.getInstance().unDisposable();
    }

    public void onProgress(long writtenBytesCount, long totalBytesCount) {
    }

    public abstract void onSuccess();

    public abstract void onError(String msg);

//    public abstract void addDisposable(Disposable d);

}
