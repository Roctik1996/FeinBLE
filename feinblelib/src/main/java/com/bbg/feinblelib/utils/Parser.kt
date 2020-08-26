package com.bbg.feinblelib.utils

import com.bbg.feinblelib.utils.Const.keyForStatus
import com.bbg.feinblelib.utils.Const.keyForStatusCharging
import com.bbg.feinblelib.utils.Const.keyForStatusHMI
import com.bbg.feinblelib.utils.Utils.convertBinaryToDecimal
import com.bbg.feinblelib.utils.Utils.intToBinary
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.HashMap
import kotlin.experimental.inv
import kotlin.math.roundToInt


object Parser {
    /**
     * Response parser
     *
     * @param data - array of bytes
     *
     * @return parsed value
     */
    @ExperimentalUnsignedTypes
    fun parseCommand(data: ByteArray, isDeviceInfo: Boolean): HashMap<*, *> {
        val parseResult = HashMap<String?, String>()
        val result = StringBuilder()
        logResponse(data)

        if (!isDeviceInfo)
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
                        "0x0080" -> {
                            parseResult.putAll(getValues(data, *Const.batteryLogMemory))
                            val batteryCapacityBit = StringBuilder()
                            batteryCapacityBit.append(intToBinary(parseResult[Const.batteryLogMemory[0]].toString().toInt()))
                                    .append(intToBinary(parseResult[Const.batteryLogMemory[1]].toString().toInt()))
                            parseResult[Const.batteryLogMemory[2]] = convertBinaryToDecimal(batteryCapacityBit.toString())
                            return parseResult
                        }
                        "0x0082" -> {
                            parseResult.putAll(getValues(data, *Const.batterySetsStored))
                            val batteryCapacityBit = StringBuilder()
                            batteryCapacityBit.append(intToBinary(parseResult[Const.batterySetsStored[0]].toString().toInt()))
                                    .append(intToBinary(parseResult[Const.batterySetsStored[1]].toString().toInt()))
                            parseResult[Const.batterySetsStored[2]] = convertBinaryToDecimal(batteryCapacityBit.toString())
                            return parseResult
                        }
                        "0x0084" -> {
                            parseResult.putAll(getValues(data, *Const.keyForSets))
                            val parameterBit = StringBuilder()
                            parameterBit.append(intToBinary(parseResult["PARAMETER_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["PARAMETER_LSB"].toString().toInt()))
                            parseResult["PARAMETER_MSB_LSB"] = convertBinaryToDecimal(parameterBit.toString())

                            val year = Calendar.getInstance().get(Calendar.YEAR)
                            val yyyy = StringBuilder().append(year.toString().substring(0, 2))
                                    .append(parseResult["MANUFACTURING_DATE_YEAR"].toString())
                            val mm = String.format("%02d", (parseResult["MANUFACTURING_DATE_WEEK"].toString().toInt() / 4.3).roundToInt())

                            val serialNumBit = StringBuilder()
                            serialNumBit.append(intToBinary(parseResult["SERIAL_NUMBER_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["SERIAL_NUMBER_LSB"].toString().toInt()))
                            val xxxxxx = StringBuilder().append("2").append(String.format("%05d", convertBinaryToDecimal(serialNumBit.toString()).toInt()))
                            parseResult["SERIAL_NUMBER"] = StringBuilder().append(yyyy).append(mm).append(xxxxxx).toString()

                            val batteryCapacityBit = StringBuilder()
                            batteryCapacityBit.append(intToBinary(parseResult["BATTERY_CAPACITY_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["BATTERY_CAPACITY_LSB"].toString().toInt()))
                            parseResult["BATTERY_CAPACITY"] = convertBinaryToDecimal(batteryCapacityBit.toString())

                            val chargingCyclesBit = StringBuilder()
                            chargingCyclesBit.append(intToBinary(parseResult["CHARGING_CYCLES_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["CHARGING_CYCLES_LSB"].toString().toInt()))
                            parseResult["CHARGING_CYCLES"] = convertBinaryToDecimal(chargingCyclesBit.toString())

                            val batteryVoltageBit = StringBuilder()
                            batteryVoltageBit.append(intToBinary(parseResult["BATTERY_VOLTAGE_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["BATTERY_VOLTAGE_LSB"].toString().toInt()))
                            parseResult["BATTERY_VOLTAGE"] = convertBinaryToDecimal(batteryVoltageBit.toString())
                            return parseResult
                        }
                        "0x0086" -> return getBitValues(getValues(data, *Const.keyForCurrentBatteryData))
                        "0x0090" -> {
                            parseResult.putAll(getValues(data, *Const.keyForCurrentChargerData))

                            val hardwareBit = StringBuilder()
                            hardwareBit.append(intToBinary(parseResult["HARDWARE_VERSION_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["HARDWARE_VERSION_LSB"].toString().toInt()))
                            parseResult["HARDWARE_VERSION"] = convertBinaryToDecimal(hardwareBit.toString())

                            val firmwareBit = StringBuilder()
                            firmwareBit.append(intToBinary(parseResult["FIRMWARE_VERSION_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["FIRMWARE_VERSION_MSB"].toString().toInt()))
                            parseResult["FIRMWARE_VERSION"] = convertBinaryToDecimal(firmwareBit.toString())

                            val serialBit = StringBuilder()
                            serialBit.append(intToBinary(parseResult["SERIAL_NUMBER_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["SERIAL_NUMBER_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["SERIAL_NUMBER_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["SERIAL_NUMBER_LSB_LAST"].toString().toInt()))
                            parseResult["SERIAL_NUMBER"] = StringBuilder().append("20").append(String.format("%10s",convertBinaryToDecimal(serialBit.toString())).replace(" ","0")).toString()

                            val mainsBit = StringBuilder()
                            mainsBit.append(intToBinary(parseResult["N_OF_MAINS_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["N_OF_MAINS_LSB"].toString().toInt()))
                            parseResult["N_OF_MAINS"] = convertBinaryToDecimal(mainsBit.toString())

                            val pluggedBit = StringBuilder()
                            pluggedBit.append(intToBinary(parseResult["N_OF_PLUGGED_BATTERIES_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["N_OF_PLUGGED_BATTERIES_LSB"].toString().toInt()))
                            parseResult["N_OF_PLUGGED_BATTERIES"] = convertBinaryToDecimal(pluggedBit.toString())

                            val chargedBit = StringBuilder()
                            chargedBit.append(intToBinary(parseResult["N_OF_CHARGED_BATTERIES_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["N_OF_CHARGED_BATTERIES_LSB"].toString().toInt()))
                            parseResult["N_OF_CHARGED_BATTERIES"] = convertBinaryToDecimal(chargedBit.toString())

                            val chg1Bit = StringBuilder()
                            chg1Bit.append(intToBinary(parseResult["N_OF_PART_CHG_1_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["N_OF_PART_CHG_1_LSB"].toString().toInt()))
                            parseResult["N_OF_PART_CHG_1"] = convertBinaryToDecimal(chg1Bit.toString())

                            val chg2Bit = StringBuilder()
                            chg2Bit.append(intToBinary(parseResult["N_OF_PART_CHG_2_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["N_OF_PART_CHG_2_LSB"].toString().toInt()))
                            parseResult["N_OF_PART_CHG_2"] = convertBinaryToDecimal(chg2Bit.toString())

                            val fanBit = StringBuilder()
                            fanBit.append(intToBinary(parseResult["FAN_ON_COUNTER_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["FAN_ON_COUNTER_LSB"].toString().toInt()))
                            parseResult["FAN_ON_COUNTER"] = convertBinaryToDecimal(fanBit.toString())

                            val fanTotalBit = StringBuilder()
                            fanTotalBit.append(intToBinary(parseResult["FAN_TOTAL_ON_TIME_MSB"].toString().toInt()))
                                    .append(intToBinary(parseResult["FAN_TOTAL_ON_TIME_LSB"].toString().toInt()))
                            parseResult["FAN_TOTAL_ON_TIME"] = convertBinaryToDecimal(fanTotalBit.toString())

                            val totalBit = StringBuilder()
                            totalBit.append(intToBinary(parseResult["TOTAL_ON_TIME_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TOTAL_ON_TIME_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TOTAL_ON_TIME_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TOTAL_ON_TIME_LSB_LAST"].toString().toInt()))
                            parseResult["TOTAL_ON_TIME"] = convertBinaryToDecimal(totalBit.toString())

                            val batteryPluggedBit = StringBuilder()
                            batteryPluggedBit.append(intToBinary(parseResult["TIME_BATTERY_PLUGGED_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_BATTERY_PLUGGED_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_BATTERY_PLUGGED_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_BATTERY_PLUGGED_LSB_LAST"].toString().toInt()))
                            parseResult["TIME_BATTERY_PLUGGED"] = convertBinaryToDecimal(batteryPluggedBit.toString())

                            val waitingBit = StringBuilder()
                            waitingBit.append(intToBinary(parseResult["WAITING_TIME_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["WAITING_TIME_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["WAITING_TIME_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["WAITING_TIME_LSB_LAST"].toString().toInt()))
                            parseResult["WAITING_TIME"] = convertBinaryToDecimal(waitingBit.toString())

                            val ccBit = StringBuilder()
                            ccBit.append(intToBinary(parseResult["TIME_CC_CHARGE_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_CC_CHARGE_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_CC_CHARGE_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_CC_CHARGE_LSB_LAST"].toString().toInt()))
                            parseResult["TIME_CC_CHARGE"] = convertBinaryToDecimal(ccBit.toString())

                            val cvBit = StringBuilder()
                            cvBit.append(intToBinary(parseResult["TIME_CV_CHARGE_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_CV_CHARGE_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_CV_CHARGE_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_CV_CHARGE_LSB_LAST"].toString().toInt()))
                            parseResult["TIME_CV_CHARGE"] = convertBinaryToDecimal(cvBit.toString())

                            val _1wBit = StringBuilder()
                            _1wBit.append(intToBinary(parseResult["TIME_1W_25W_LOAD_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_1W_25W_LOAD_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_1W_25W_LOAD_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_1W_25W_LOAD_LSB_LAST"].toString().toInt()))
                            parseResult["TIME_1W_25W_LOAD"] = convertBinaryToDecimal(_1wBit.toString())

                            val _25wBit = StringBuilder()
                            _25wBit.append(intToBinary(parseResult["TIME_25W_75W_LOAD_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_25W_75W_LOAD_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_25W_75W_LOAD_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_25W_75W_LOAD_LSB_LAST"].toString().toInt()))
                            parseResult["TIME_25W_75W_LOAD"] = convertBinaryToDecimal(_25wBit.toString())

                            val _75wBit = StringBuilder()
                            _75wBit.append(intToBinary(parseResult["TIME_75W_140W_LOAD_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_75W_140W_LOAD_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_75W_140W_LOAD_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_75W_140W_LOAD_LSB_LAST"].toString().toInt()))
                            parseResult["TIME_75W_140W_LOAD"] = convertBinaryToDecimal(_75wBit.toString())

                            val maxBit = StringBuilder()
                            maxBit.append(intToBinary(parseResult["TIME_MAX_LOAD_MSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_MAX_LOAD_MSB_LAST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_MAX_LOAD_LSB_FIRST"].toString().toInt()))
                                    .append(intToBinary(parseResult["TIME_MAX_LOAD_LSB_LAST"].toString().toInt()))
                            parseResult["TIME_75W_140W_LOAD"] = convertBinaryToDecimal(maxBit.toString())

                            return parseResult
                        }
                        "0x000" -> {
                            parseResult["ERROR"] = "CS error / command not OK"
                            return parseResult
                        }

                        "0x000A" -> {
                            parseResult["SUCCESS"] = "success"
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
        else {
            val hexResponse = StringBuilder()
            for (k in data.indices)
                hexResponse.append(String.format("%02X", data[k]))
            val buff: ByteBuffer = ByteBuffer.allocate(hexResponse.toString().length / 2)

            var i = 0
            while (i < hexResponse.toString().length) {
                buff.put(hexResponse.toString().substring(i, i + 2).toInt(16).toByte())
                i += 2
            }
            buff.rewind()
            val cs: Charset = Charset.forName("UTF-8")
            val cb: CharBuffer = cs.decode(buff)
            parseResult["VALUE"] = cb.toString()
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
    private fun getBitValues(data: HashMap<String, String>): HashMap<*, *> {
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

        val statusData = (data["STATUS"]!!.toUByte())
        val statusResult = HashMap<String, String>()
        val status = String.format("%8s", Integer.toBinaryString(statusData.toInt() and 0xFF)).replace(' ', '0')
        val bitStatusArray = status.toCharArray()
        for (i in bitStatusArray.indices)
            statusResult[keyForStatus[i]] = bitStatusArray[i].toString()

        val parseResult = HashMap<String, String>()
        parseResult.putAll(data)
        parseResult.putAll(statusChargerResult)
        parseResult.putAll(statusHMIResult)
        parseResult.putAll(statusResult)

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val yyyy = StringBuilder().append(year.toString().substring(0, 2))
                .append(parseResult["MANUFACTURING_DATE_YEAR"].toString())
        val mm = String.format("%02d", (parseResult["MANUFACTURING_DATE_WEEK"].toString().toInt() / 4.3).roundToInt())

        val serialNumBit = StringBuilder()
        serialNumBit.append(intToBinary(parseResult["SERIAL_NUMBER_MSB"].toString().toInt()))
                .append(intToBinary(parseResult["SERIAL_NUMBER_LSB"].toString().toInt()))
        val xxxxxx = StringBuilder().append("2").append(String.format("%05d", convertBinaryToDecimal(serialNumBit.toString()).toInt()))
        parseResult["SERIAL_NUMBER"] = StringBuilder().append(yyyy).append(mm).append(xxxxxx).toString()


        val batteryCapacityBit = StringBuilder()
        batteryCapacityBit.append(intToBinary(parseResult["BATTERY_CAPACITY_MSB"].toString().toInt()))
                .append(intToBinary(parseResult["BATTERY_CAPACITY_LSB"].toString().toInt()))
        parseResult["BATTERY_CAPACITY"] = convertBinaryToDecimal(batteryCapacityBit.toString())

        val chargingCyclesBit = StringBuilder()
        chargingCyclesBit.append(intToBinary(parseResult["CHARGING_CYCLES_MSB"].toString().toInt()))
                .append(intToBinary(parseResult["CHARGING_CYCLES_LSB"].toString().toInt()))
        parseResult["CHARGING_CYCLES"] = convertBinaryToDecimal(chargingCyclesBit.toString())

        val batteryVoltageBit = StringBuilder()
        batteryVoltageBit.append(intToBinary(parseResult["ACTUAL_BATTERY_VOLTAGE_MSB"].toString().toInt()))
                .append(intToBinary(parseResult["ACTUAL_BATTERY_VOLTAGE_LSB"].toString().toInt()))
        parseResult["ACTUAL_BATTERY_VOLTAGE"] = convertBinaryToDecimal(batteryVoltageBit.toString())

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
