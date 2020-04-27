package com.bbg.feinblelib.bluetooth

import android.bluetooth.BluetoothDevice
import android.os.Build
import com.bbg.feinblelib.BleManager
import com.bbg.feinblelib.data.BleDevice
import com.bbg.feinblelib.utils.BleLruHashMap
import java.util.*

class MultipleBluetoothController {
    private val bleLruHashMap: BleLruHashMap<String?, BleBluetooth?> = BleLruHashMap(BleManager.instance.maxConnectCount)
    private val bleTempHashMap: HashMap<String, BleBluetooth> = HashMap()

    @Synchronized
    fun buildConnectingBle(bleDevice: BleDevice?): BleBluetooth {
        val bleBluetooth = BleBluetooth(bleDevice!!)
        if (!bleTempHashMap.containsKey(bleBluetooth.deviceKey)) {
            bleTempHashMap[bleBluetooth.deviceKey] = bleBluetooth
        }
        return bleBluetooth
    }

    @Synchronized
    fun removeConnectingBle(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (bleTempHashMap.containsKey(bleBluetooth.deviceKey)) {
            bleTempHashMap.remove(bleBluetooth.deviceKey)
        }
    }

    @Synchronized
    fun addBleBluetooth(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (!bleLruHashMap.containsKey(bleBluetooth.deviceKey)) {
            bleLruHashMap[bleBluetooth.deviceKey] = bleBluetooth
        }
    }

    @Synchronized
    fun removeBleBluetooth(bleBluetooth: BleBluetooth?) {
        if (bleBluetooth == null) {
            return
        }
        if (bleLruHashMap.containsKey(bleBluetooth.deviceKey)) {
            bleLruHashMap.remove(bleBluetooth.deviceKey)
        }
    }

    @Synchronized
    fun isContainDevice(bleDevice: BleDevice?): Boolean {
        return bleDevice != null && bleLruHashMap.containsKey(bleDevice.key)
    }

    @Synchronized
    fun isContainDevice(bluetoothDevice: BluetoothDevice?): Boolean {
        return bluetoothDevice != null && bleLruHashMap.containsKey(bluetoothDevice.name + bluetoothDevice.address)
    }

    @Synchronized
    fun getBleBluetooth(bleDevice: BleDevice?): BleBluetooth? {
        if (bleDevice != null && bleLruHashMap.containsKey(bleDevice.key)) {
                return bleLruHashMap[bleDevice.key]
        }
        return null
    }

    @Synchronized
    fun disconnect(bleDevice: BleDevice?) {
        if (isContainDevice(bleDevice)) {
            getBleBluetooth(bleDevice)!!.disconnect()
        }
    }

    @Synchronized
    fun disconnectAllDevice() {
        for ((_, value) in bleLruHashMap) {
            value!!.disconnect()
        }
        bleLruHashMap.clear()
    }

    @Synchronized
    fun destroy() {
        for ((_, value) in bleLruHashMap) {
            value!!.destroy()
        }
        bleLruHashMap.clear()
        for ((_, value) in bleTempHashMap) {
            value.destroy()
        }
        bleTempHashMap.clear()
    }

    @get:Synchronized
    val bleBluetoothList: List<BleBluetooth?>
        get() {
            val bleBluetoothList: List<BleBluetooth?> = ArrayList(bleLruHashMap.values)
            Collections.sort(bleBluetoothList) { lhs, rhs -> rhs?.deviceKey?.let { lhs?.deviceKey?.compareTo(it, ignoreCase = true) }!! }
            return bleBluetoothList
        }

    @get:Synchronized
    val deviceList: List<BleDevice>
        get() {
            refreshConnectedDevice()
            val deviceList: MutableList<BleDevice> = ArrayList()
            for (BleBluetooth in bleBluetoothList) {
                if (BleBluetooth != null) {
                    deviceList.add(BleBluetooth.device)
                }
            }
            return deviceList
        }

    private fun refreshConnectedDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            val bluetoothList = bleBluetoothList
            var i = 0
            while (i < bluetoothList.size) {
                val bleBluetooth = bluetoothList[i]
                if (!BleManager.instance.isConnected(bleBluetooth!!.device)) {
                    removeBleBluetooth(bleBluetooth)
                }
                i++
            }
        }
    }

}