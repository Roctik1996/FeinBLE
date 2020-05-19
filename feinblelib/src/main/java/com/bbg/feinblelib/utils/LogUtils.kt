package com.bbg.feinblelib.utils

object LogUtils {
    lateinit var command: ByteArray
    var response:String = ""

    @ExperimentalUnsignedTypes
    fun getCommand():String{
        val hexResponse = StringBuilder()
        for (k in command.indices)
            hexResponse.append((command[k].toUByte() and 255u).toString(16))
        BleLog.i("response: $hexResponse")
        return hexResponse.toString()
    }
}