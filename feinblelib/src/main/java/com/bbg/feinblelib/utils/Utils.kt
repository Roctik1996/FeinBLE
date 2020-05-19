package com.bbg.feinblelib.utils

import kotlin.experimental.inv

object Utils {
    /**
     * add checksum to command
     *
     * @param data - array of bytes(without checksum)
     * @return array with checksum
     */
    private fun addChecksumToCommand(n: Int, data: ByteArray, cs: Byte): ByteArray {
        val csData = ByteArray(n + 1)
        if (n >= 0) System.arraycopy(data, 0, csData, 0, n)
        csData[n] = cs
        LogUtils.command=csData
        return csData
    }

    /**
     * calculated checksum
     *
     * @param data - array of bytes(without checksum)
     * @return array with checksum
     */
    fun dataWithChecksum(data: ByteArray): ByteArray {
        var sum: Byte = 0
        for (b in data) {
            sum = (sum + b).toByte()
        }
        val cs = (sum.inv() + 1).toByte()
        return addChecksumToCommand(data.size, data, cs)
    }
}