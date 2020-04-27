package com.bbg.feinblelib.utils

import android.util.Log

object BleLog {
    var isPrint = true
    private const val defaultTag = "Ble"
    fun d(msg: String?) {
        if (isPrint && msg != null) Log.d(defaultTag, msg)
    }

    fun i(msg: String?) {
        if (isPrint && msg != null) Log.i(defaultTag, msg)
    }

    fun w(msg: String?) {
        if (isPrint && msg != null) Log.w(defaultTag, msg)
    }

    fun e(msg: String?) {
        if (isPrint && msg != null) Log.e(defaultTag, msg)
    }
}