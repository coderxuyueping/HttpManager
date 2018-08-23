package com.halove.xyp.apilibrary;

import android.util.ArrayMap;
import android.util.Log;


import com.halove.xyp.apilibrary.download.ProgressResponseBody;
import com.halove.xyp.apilibrary.upload.ProgressObserver;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by xyp on 2018/3/13.
 */

public class RetrofitHelper {
    private static RetrofitHelper retrofitHelper;
    private Retrofit retrofit;
    private OkHttpClient okHttpClient;
    private static final int DEFAULT_TIME_OUT = 10;
    private boolean addQueryParameter;//暂时不用这个添加公共参数
    private boolean addHeader;
    private boolean addDownloadInterceptor;
    private ProgressObserver observer;

    private boolean openLog;

    private Map<String, String> queryParameter;
    private Map<String, String> headParameter;


    private RetrofitHelper() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS);

        if (openLog) {//如果当前是debug模式就开启日志过滤器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request oldRequest = chain.request();
                if (!addQueryParameter) {
                    addQueryParameter = false;
                    return chain.proceed(oldRequest);
                }
                // 添加新的参数,公共参数
                HttpUrl.Builder authorizedUrlBuilder = oldRequest.url()
                        .newBuilder()
                        .scheme(oldRequest.url().scheme())
                        .host(oldRequest.url().host());
                for(Map.Entry<String, String> entry : queryParameter.entrySet()){
                    Log.d("entry", "key:"+entry.getKey()+"||value:"+entry.getValue());
                    authorizedUrlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
                }


                // 新的请求
                Request newRequest = oldRequest.newBuilder()
                        .method(oldRequest.method(), oldRequest.body())
                        .url(authorizedUrlBuilder.build())
                        .build();

                //打印添加新的参数后的完整url
                Log.d("xudaha", newRequest.url().toString());
                addQueryParameter = false;

                return chain.proceed(newRequest);
            }
        });

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (!addHeader) {
                    addHeader = false;
                    return chain.proceed(chain.request());
                }
                //添加head
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder();
                //设置具体的header内容
                for(Map.Entry<String, String> entry : headParameter.entrySet()){
                    builder.header(entry.getKey(), entry.getValue());
                }


                Request.Builder requestBuilder =
                        builder.method(originalRequest.method(), originalRequest.body());
                Request request = requestBuilder.build();
                addHeader = false;
                return chain.proceed(request);
            }
        });

        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (!addDownloadInterceptor) {
                    addDownloadInterceptor = false;
                    return chain.proceed(chain.request());
                }
                addDownloadInterceptor = false;
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), observer))
                        .build();
            }
        });

        okHttpClient = builder.build();
    }

    public static RetrofitHelper getInstance() {
        if (retrofitHelper == null) {
            synchronized (HttpManager.class) {
                if (retrofitHelper == null)
                    retrofitHelper = new RetrofitHelper();
            }
        }
        return retrofitHelper;
    }

    public BaseService getService() {
        return getRetrofit().create(BaseService.class);
    }

    public Retrofit getRetrofit() {
        if (retrofit == null) {
            synchronized (Retrofit.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(ApiConstant.BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public RetrofitHelper addQueryParameter(Map<String, String> params) throws NullPointerException {
        addQueryParameter = true;
        if(queryParameter == null)
            queryParameter = new ArrayMap<>();
        else
            queryParameter.clear();
        if(params == null)
            throw new NullPointerException("params can not be null");
        else
            queryParameter.putAll(params);
        return this;
    }

    public RetrofitHelper addHeader(Map<String, String> params) throws NullPointerException {
        addHeader = true;
        if(headParameter == null)
            headParameter = new ArrayMap<>();
        else
            headParameter.clear();
        if(params == null)
            throw new NullPointerException("params can not be null");
        else
            headParameter.putAll(params);
        return this;
    }

    /**
     * 添加默认参数
     * @return
     */
    public RetrofitHelper addQueryParameter(){
        addQueryParameter = true;
        if(queryParameter == null)
            queryParameter = new ArrayMap<>();
        else
            queryParameter.clear();
        return this;
    }

    public RetrofitHelper addHeader(){
        addHeader = true;
        if(headParameter == null)
            headParameter = new ArrayMap<>();
        else
            headParameter.clear();
        return this;
    }

    public RetrofitHelper addDownloadInterceptor(ProgressObserver observer) {
        if (observer != null) {
            addDownloadInterceptor = true;
            this.observer = observer;
        }
        return this;
    }

    public void openLog(boolean open){
        this.openLog = open;
    }
}
