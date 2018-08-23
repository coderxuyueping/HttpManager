package com.halove.xyp.apilibrary.observer;

import com.halove.xyp.apilibrary.ApiConstant;
import com.halove.xyp.apilibrary.BaseResponse;
import com.halove.xyp.apilibrary.utils.GsonUtil;

import java.util.List;

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

    //可以根据具体需求替换BaseResponse解析json
    @Override
    protected void parseJson(String res) {
//        super.parseJson(res);
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
}
