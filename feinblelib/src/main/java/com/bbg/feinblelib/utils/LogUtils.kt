package com.bbg.feinblelib.utils

object LogUtils {
    var command: ByteArray = ByteArray(1)
    var response: String = ""

    @ExperimentalUnsignedTypes
    fun getCommand(): String {
        val hexResponse = StringBuilder()
        if (command.isNotEmpty()) {
            for (b in command) {
                hexResponse.append(String.format("%02X", b))
            }
            BleLog.i("command: $hexResponse")
        } else
            hexResponse.append("")

        return hexResponse.toString()
    }
}