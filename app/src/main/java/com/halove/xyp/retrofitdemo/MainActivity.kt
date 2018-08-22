package com.halove.xyp.retrofitdemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.util.ArrayMap
import android.util.Log
import android.view.View
import com.halove.xyp.apilibrary.observer.ArrayObserver
import com.halove.xyp.apilibrary.HttpManager
import com.halove.xyp.apilibrary.observer.ObjectObserver
import com.halove.xyp.apilibrary.RxLifeCycle
import com.halove.xyp.apilibrary.upload.ProgressObserver
import com.halove.xyp.apilibrary.utils.FileUtil
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    /**
     * 返回jsonObject
     */

    fun getObject(view: View){
        val map = ArrayMap<String, String>()
        map.put("part", "1")
        map.put("page", "1")
        //解析jsonObject
        HttpManager.getInstance()
                .executeGet("http://room.9158.com/live/part_list_11.aspx", map)
                .subscribe(object : ObjectObserver<RoomList>(RoomList::class.java){
                    override fun onSuccess(t: RoomList?) {
                        //todo 处理数据
                        Log.d("xuyueping", t.toString())
                    }

                    override fun onError(msg: String?) {
                        toast(msg?:"未知错误")
                    }
                })
    }


    /**
     * 返回jsonArray
     */
    fun getArray(view: View){
        HttpManager.getInstance().executeGet("http://room.9158.com/live/get_viewinfo_new.aspx")
                .subscribe(object : ArrayObserver<Title>(Array<Title>::class.java){
                    override fun onSuccess(t: MutableList<Title>?) {
                        //todo 处理数据
                        Log.d("xuyueping", t.toString())
                    }

                    override fun onError(msg: String?) {
                        toast(msg?:"未知错误")
                    }

                })
    }


    /**
     * 带进度条的下载
     */
    fun download(view: View){
        HttpManager.getInstance().downLoadFile("http://mobile.9158.com/9158ChatRoom.apk",
                FileUtil.getCacheFileByType(this, "apk_file").absolutePath
                , "9158.apk", object : ProgressObserver(true){

            override fun onProgress(writtenBytesCount: Long, totalBytesCount: Long) {
                progress.max = totalBytesCount.toInt()
                progress.progress = writtenBytesCount.toInt()
            }

            override fun onSuccess() {
                toast("下载成功")
            }

            override fun onError(msg: String?) {
                toast(msg?:"未知错误")
            }
        })
    }

    /**
     * 帶进度条的上传
     */
    fun upLoad(){
        HttpManager.getInstance().upLoadFile("url", null, File(""),
                object : ProgressObserver() {
                    override fun onSuccess() {
                    }

                    override fun onError(msg: String?) {
                    }

                    override fun onProgress(writtenBytesCount: Long, totalBytesCount: Long) {
                    }

                })
    }



    /**
     * 加密的post，加密流放在RequestBody里,这里采用的是AES加密，可根据具体需求后续再扩展
     */
    fun aesPost(){
        var params = ArrayMap<String, String>()
        params.put("","")

        //公共参数通过拼接集合的形式
        HttpManager.getInstance().addCommonParams(params)
                .executeAesPost("url", params)
                .subscribe(object : ObjectObserver<ResponseBody>(ResponseBody::class.java){
                    override fun onSuccess(t: ResponseBody?) {
                    }

                    override fun onError(msg: String?) {
                    }

                })

        //开启拦截器，在拦截器里添加公共参数
        HttpManager.getInstance().addQueryParameter()
                .executeAesPost("url", params)
                .subscribe(object : ObjectObserver<ResponseBody>(ResponseBody::class.java){
                    override fun onSuccess(t: ResponseBody?) {
                    }

                    override fun onError(msg: String?) {
                    }

                })

    }


    override fun onDestroy() {
        super.onDestroy()
        //这里最好手动释放一次，防止请求过程中退出
        RxLifeCycle.getInstance().unDisposable()
    }
}
