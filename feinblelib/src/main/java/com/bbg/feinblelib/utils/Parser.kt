package com.bbg.feinblelib.utils

import com.bbg.feinblelib.utils.Const.keyForStatusCharging
import com.bbg.feinblelib.utils.Const.keyForStatusHMI
import kotlin.experimental.and
import kotlin.experimental.inv


object Parser {
    /**
     * Response parser
     *
     * @param data - array of bytes
     *
     * @return parsed value
     */
    @ExperimentalUnsignedTypes
    fun parseCommand(data: ByteArray): HashMap<*, *> {
        val parseResult = HashMap<String?, String>()
        val result = StringBuilder()
        logResponse(data)
        if (data[data.size - 1] == getChecksum(data)) {
            if (getCommand(data.toUByteArray()) == getCommand(LogUtils.command.toUByteArray())) {
                when (getCommand(data.toUByteArray())) {
                    "0x0010" -> {
                        var i = 5
                        while (i < getLastNonZeroIndex(data)) {
                            result.append(data[i].toUByte() and 255u).append(".")
                            i++
                        }
                        parseResult[Const.protocol] = result.toString()
                        return parseResult
                    }
                    "0x0040" -> {
                        var i = 6
                        while (i < getLastNonZeroIndex(data)) {
                            result.append(data[i].toUByte() and 255u)
                            i++
                        }
                        parseResult[Const.chargingMode] = result.toString()
                        return parseResult
                    }
                    "0x0041" -> {
                        var i = 6
                        while (i < getLastNonZeroIndex(data)) {
                            parseResult[Const.currentChargingMode[i - 6]] = (data[i].toUByte() and 255u).toString()
                            i++
                        }
                        return parseResult
                    }
                    "0x0080" -> return getValues(data, *Const.batteryLogMemory)
                    "0x0082" -> return getValues(data, *Const.batterySetsStored)
                    "0x0084" -> return getValues(data, *Const.keyForSets)
                    "0x0086" -> return getBitValues(getValues(data, *Const.keyForCurrentBatteryData))
                    "0x0090" -> return getValues(data, *Const.keyForCurrentChargerData)
                    "0x000" -> {
                        parseResult["ERROR"] = "CS error / command not OK"
                        return parseResult
                    }
                    else -> {
                        parseResult["ERROR"] = "command not OK"
                        return parseResult
                    }
                }
            } else {
                parseResult["ERROR"] = "command not OK"
                return parseResult
            }
        } else {
            BleLog.w("CS not OK")
            parseResult["ERROR"] = "CS not OK"
            return parseResult
        }
        return parseResult
    }

    private fun getLastNonZeroIndex(data: ByteArray): Int {
        var indexNotZero = 0
        for (k in data.indices)
            if (data[k].toInt() != 0) {
                indexNotZero = k
            }
        return indexNotZero
    }

    @ExperimentalUnsignedTypes
    fun logResponse(data: ByteArray) {
        val hexResponse = StringBuilder()

        for (k in data.indices)
            hexResponse.append(String.format("%02X", data[k]))
        BleLog.i("response: $hexResponse")
        LogUtils.response = hexResponse.toString()
    }

    @ExperimentalUnsignedTypes
    private fun getValues(data: ByteArray, vararg keys: String): HashMap<String, String> {
        val parseResult = HashMap<String, String>()
        for ((k, i) in (5 until getLastNonZeroIndex(data)).withIndex()) {
            parseResult[keys[k]] = (data[i].toUByte() and 255u).toString()
        }
        return parseResult
    }

    @ExperimentalUnsignedTypes
    private fun getBitValues(data: HashMap<String,String>): HashMap<*, *> {
        val statusChargingData = (data["STATUS_CHARGING"]!!.toUByte())
        val statusChargerResult = HashMap<String, String>()
        val chargerStatus = String.format("%8s", Integer.toBinaryString(statusChargingData.toInt() and 0xFF)).replace(' ', '0')
        val bitStatusChargerArray = chargerStatus.toCharArray()
        for (i in bitStatusChargerArray.indices)
            statusChargerResult[keyForStatusCharging[i]] = bitStatusChargerArray[i].toString()

        val statusHMIData = (data["STATUS_HMI"]!!.toUByte())
        val statusHMIResult = HashMap<String, String>()
        val HMIStatus = String.format("%8s", Integer.toBinaryString(statusHMIData.toInt() and 0xFF)).replace(' ', '0')
        val bitStatusHMIArray = HMIStatus.toCharArray()
        for (i in bitStatusHMIArray.indices)
            statusHMIResult[keyForStatusHMI[i]] = bitStatusHMIArray[i].toString()

        val parseResult = HashMap<String, String>()
        parseResult.putAll(data)
        parseResult.putAll(statusChargerResult)
        parseResult.putAll(statusHMIResult)

        return parseResult
    }

    private fun getChecksum(data: ByteArray): Byte {
        var sum: Byte = 0
        for (i in 0 until data.size - 1) {
            sum = (sum + data[i]).toByte()
        }
        return ((sum.inv() + 1).toByte())
    }

    @ExperimentalUnsignedTypes
    private fun getCommand(data: UByteArray): String {
        var cmd = ""
        cmd = "0x0" + Integer.toHexString(data[3].toInt()) + Integer.toHexString((data[4]).toInt())
        return cmd
    }
}
