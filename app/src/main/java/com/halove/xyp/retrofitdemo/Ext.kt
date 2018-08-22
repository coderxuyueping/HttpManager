package com.halove.xyp.retrofitdemo

import android.content.Context
import android.widget.Toast

/**
 * Created by xyp on 2018/8/22.
 */
fun Context.toast(msg: String, time: Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, msg, time).show()
}