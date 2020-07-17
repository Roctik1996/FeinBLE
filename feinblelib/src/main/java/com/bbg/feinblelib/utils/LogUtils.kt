package com.bbg.feinblelib.utils

object LogUtils {
    lateinit var command: ByteArray
    var response:String = ""

    @ExperimentalUnsignedTypes
    fun getCommand():String{
        val hexResponse = StringBuilder()
        for (b in command) {
            hexResponse.append(String.format("%02X", b))
        }
        BleLog.i("response: $hexResponse")
        return hexResponse.toString()
    }
}