package com.halove.xyp.apilibrary;


import android.util.ArrayMap;

import com.google.gson.Gson;
import com.halove.xyp.apilibrary.upload.ProgressObserver;
import com.halove.xyp.apilibrary.upload.ProgressRequestBody;
import com.halove.xyp.apilibrary.security.AesEncrypt;
import com.halove.xyp.apilibrary.utils.FileUtil;

import java.io.File;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by xyp on 2018/3/13.
 */

public class HttpManager {
    private static HttpManager httpManager;
    private static final String key = "mlno3xgfl4z76f31fr51xtjhkj87uh1t";

    private HttpManager() {
    }

    public static HttpManager getInstance() {
        if (httpManager == null) {
            synchronized (HttpManager.class) {
                if (httpManager == null)
                    httpManager = new HttpManager();
            }
        }
        return httpManager;
    }


    public <T> ObservableTransformer<T, T> schedulersTransformer() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public Observable<ResponseBody> executeGet(String url) {
        return RetrofitHelper.getInstance().getService()
                .executeGet(url)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    public Observable<ResponseBody> executeGet(String url, Map<String, String> parameters) {
        return RetrofitHelper.getInstance().getService()
                .executeGet(url, parameters)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    public Observable<ResponseBody> executePost(String url) {
        return RetrofitHelper.getInstance().getService()
                .executePost(url)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    public Observable<ResponseBody> executePost(String url, Map<String, String> parameters) {
        return RetrofitHelper.getInstance().getService()
                .executePost(url, parameters)
                .compose(this.<ResponseBody>schedulersTransformer());
    }

    /**
     * 加密的post
     *
     * @param url
     * @param params
     * @return
     */
    public Observable<ResponseBody> executeAesPost(String url, Map<String, String> params) {
        byte[] bytes = AesEncrypt.getInstance().encrypt(key.getBytes(), new Gson().toJson(params));
        RequestBody body = FormBody.create(MediaType.parse("application/x-www-form-urlencoded"), bytes);
        return RetrofitHelper.getInstance().getService()
                .executePost(url, body)
                .compose(this.<ResponseBody>schedulersTransformer());
    }


    public void upLoadFile(String url, Map<String, String> parameters, File file, ProgressObserver observer) {
        if(parameters == null)
            parameters = new ArrayMap<>();
        // 创建 RequestBody，用于封装构建RequestBody
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", file.getName()
                        , new ProgressRequestBody(requestFile, observer));

        // 添加描述
        String descriptionString = "文件描述";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);
        RetrofitHelper.getInstance().getService()
                .upLoadFile(url, description, parameters, body)
                .compose(this.<ResponseBody>schedulersTransformer())
                .subscribe(observer);
    }

    public void downLoadFile(String url, final String savePath, final String fileName, ProgressObserver observer) {
        RetrofitHelper.getInstance()
                .addDownloadInterceptor(observer)
                .getService()
                .downloadFile(url)
                .subscribeOn(Schedulers.io())//请求在io线程
                .observeOn(Schedulers.io())//保存文件在io线程
                .doOnNext(new Consumer<ResponseBody>() {
                    @Override
                    public void accept(ResponseBody responseBody) throws Exception {
                        FileUtil.saveFile(responseBody.byteStream(), savePath, fileName);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    //在拦截器里添加公共参数
    public HttpManager addQueryParameter() {
        RetrofitHelper.getInstance()
                .addQueryParameter();
        return this;
    }

    public HttpManager addHeader() {
        RetrofitHelper.getInstance()
                .addHeader();
        return this;
    }

    //添加公共参数,直接在Map里添加，不使用拦截器
    public HttpManager addCommonParams(Map<String, String> params) {
        if(params == null)
            params = new ArrayMap<>();
        params.put("deviceType", "android");
//        params.put("deviceid", AppUtil.getDeviceId(AppStatus.getInstance()));
//        params.put("version", AppUtil.getVersionName(AppStatus.getInstance()));
        return this;
    }
}
