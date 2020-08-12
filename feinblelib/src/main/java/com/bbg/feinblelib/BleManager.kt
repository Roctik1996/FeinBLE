package com.bbg.feinblelib

import android.annotation.TargetApi
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import com.bbg.feinblelib.bluetooth.BleBluetooth
import com.bbg.feinblelib.bluetooth.MultipleBluetoothController
import com.bbg.feinblelib.callback.*
import com.bbg.feinblelib.data.BleDevice
import com.bbg.feinblelib.data.BleScanState
import com.bbg.feinblelib.exception.BleException
import com.bbg.feinblelib.exception.OtherException
import com.bbg.feinblelib.scan.BleScanner
import com.bbg.feinblelib.utils.BleLog
import com.bbg.feinblelib.utils.Utils
import com.bbg.feinblelib.utils.Utils.convertBinaryToDecimal
import com.bbg.feinblelib.utils.Utils.intToBinary
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
open class BleManager {
    private var context: Application? = null

    /**
     * Get the BluetoothAdapter
     *
     * @return
     */
    var bluetoothAdapter: BluetoothAdapter? = null
        private set

    /**
     * Get the multiple Bluetooth Controller
     *
     * @return
     */
    var multipleBluetoothController: MultipleBluetoothController? = null
        private set

    /**
     * Get the BluetoothManager
     *
     * @return
     */
    private var bluetoothManager: BluetoothManager? = null

    /**
     * Get the maximum number of connections
     *
     * @return
     */
    var maxConnectCount = DEFAULT_MAX_MULTIPLE_DEVICE
        private set

    /**
     * Get operate timeout
     *
     * @return
     */
    var operateTimeout = DEFAULT_OPERATE_TIME
        private set

    /**
     * Get connect retry count
     *
     * @return
     */
    var reConnectCount = DEFAULT_CONNECT_RETRY_COUNT
        private set

    /**
     * Get connect retry interval
     *
     * @return
     */
    var reConnectInterval = DEFAULT_CONNECT_RETRY_INTERVAL.toLong()
        private set

    /**
     * Get operate connect Over Time
     *
     * @return
     */
    var connectOverTime = DEFAULT_CONNECT_OVER_TIME.toLong()
        private set

    private object BleManagerHolder {
        val sBleManager = BleManager()
    }

    fun init(app: Application?) {
        if (context == null && app != null) {
            context = app
            if (isSupportBle) {
                bluetoothManager = context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            }
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            multipleBluetoothController = MultipleBluetoothController()
        }
    }

    /**
     * Get the Context
     *
     * @return
     */
    fun getContext(): Context? {
        return context
    }

    /**
     * Set the maximum number of connections
     *
     * @param count
     * @return BleManager
     */
    fun setMaxConnectCount(count: Int): BleManager {
        var count = count
        if (count > DEFAULT_MAX_MULTIPLE_DEVICE) count = DEFAULT_MAX_MULTIPLE_DEVICE
        maxConnectCount = count
        return this
    }

    /**
     * Set operate timeout
     *
     * @param count
     * @return BleManager
     */
    fun setOperateTimeout(count: Int): BleManager {
        operateTimeout = count
        return this
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    fun setReConnectCount(count: Int): BleManager {
        return setReConnectCount(count, DEFAULT_CONNECT_RETRY_INTERVAL.toLong())
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    fun setReConnectCount(count: Int, interval: Long): BleManager {
        var count = count
        var interval = interval
        if (count > 10) count = 10
        if (interval < 0) interval = 0
        reConnectCount = count
        reConnectInterval = interval
        return this
    }

    /**
     * Set connect Over Time
     *
     * @param time
     * @return BleManager
     */
    fun setConnectOverTime(time: Long): BleManager {
        var time = time
        if (time <= 0) {
            time = 100
        }
        connectOverTime = time
        return this
    }

    /**
     * print log?
     *
     * @param enable
     * @return BleManager
     */
    fun enableLog(enable: Boolean): BleManager {
        BleLog.isPrint = enable
        return this
    }

    /**
     * scan device around
     *
     * @param callback
     */
    fun scan(callback: BleScanCallback?) {
        requireNotNull(callback) { "BleScanCallback can not be Null!" }
        if (!isBlueEnable) {
            BleLog.e("Bluetooth not enable!")
            callback.onScanStarted(false)
            return
        }
        BleScanner.instance.scan(serviceUuids, connectOverTime, callback)
    }

    /**
     * connect a known device
     *
     * @param bleDevice
     * @param bleGattCallback
     * @return
     */
    fun connect(bleDevice: BleDevice?, bleGattCallback: BleGattCallback?): BluetoothGatt? {
        requireNotNull(bleGattCallback) { "BleGattCallback can not be Null!" }
        if (!isBlueEnable) {
            BleLog.e("Bluetooth not enable!")
            bleGattCallback.onConnectFail(bleDevice, OtherException("Bluetooth not enable!"))
            return null
        }
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            BleLog.w("Be careful: currentThread is not MainThread!")
        }
        if (bleDevice?.device == null) {
            bleGattCallback.onConnectFail(bleDevice, OtherException("Not Found Device Exception Occurred!"))
        } else {
            val bleBluetooth = multipleBluetoothController!!.buildConnectingBle(bleDevice)
            return bleBluetooth.connect(bleDevice, bleGattCallback)
        }
        return null
    }

    /**
     * connect a device through its mac without scan,whether or not it has been connected
     *
     * @param mac
     * @param bleGattCallback
     * @return
     */
    fun connect(mac: String?, bleGattCallback: BleGattCallback?): BluetoothGatt? {
        val bluetoothDevice = bluetoothAdapter!!.getRemoteDevice(mac)
        val bleDevice = BleDevice(bluetoothDevice, 0, null, 0)
        return connect(bleDevice, bleGattCallback)
    }

    /**
     * Cancel scan
     */
    fun cancelScan() {
        BleScanner.Companion.instance.stopLeScan()
    }

    /**
     * write
     *
     * @param bleDevice
     * @param data
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun write(bleDevice: BleDevice?,
              data: ByteArray?,
              callback: BleWriteCallback?) {
        requireNotNull(callback) { "BleWriteCallback can not be Null!" }
        if (data == null) {
            BleLog.e("data is Null!")
            callback.onWriteFailure(OtherException("data is Null!"))
            return
        }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onWriteFailure(OtherException("This device not connect!"))
        } else {
            bleBluetooth.newBleConnector()
                    .withUUIDString(serviceUuid, characteristicUuid)
                    .writeCharacteristic(data, callback, characteristicUuid)
        }
    }

    /**
     * read current communication protocol version
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readProtocolVersion(bleDevice: BleDevice?,
                            callback: BleReadCallback?) {
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x10, 0x00, 0x00)
        val writeCallback: BleWriteCallback = object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                BleLog.i("command: read current communication protocol version - success")
                if (callback != null) {
                    instance.read(bleDevice, callback)
                }
            }

            override fun onWriteFailure(exception: BleException?) {
                BleLog.w("command: read current communication protocol version - fail")
                callback?.onReadFailure(exception)
            }
        }
        write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
    }

    /**
     * set charging mode / charging current
     *
     * @param bleDevice
     * @param current - charging current in percent
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun setChargingMode(bleDevice: BleDevice?,
                        current: Int,
                        callback: BleReadCallback?) {
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x40, 0x00, current.toByte())
        if (current in 1..100) {
            val writeCallback: BleWriteCallback = object : BleWriteCallback() {
                override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                    BleLog.i("command: set charging mode / charging current - success")
                    if (callback != null) {
                        instance.read(bleDevice, callback)
                    }
                }

                override fun onWriteFailure(exception: BleException?) {
                    BleLog.w("command: set charging mode / charging current - fail")
                    callback?.onReadFailure(exception)
                }
            }
            write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
        } else {
            callback?.onReadFailure(OtherException("Current value must be >=1 and <=100"))
        }
    }

    /**
     * read the current charging mode / charging current
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readChargingMode(bleDevice: BleDevice?,
                         callback: BleReadCallback?) {
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x41, 0x00, 0x00)
        val writeCallback: BleWriteCallback = object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                BleLog.i("command: read the current charging mode / charging current - success")
                if (callback != null) {
                    instance.read(bleDevice, callback)
                }
            }

            override fun onWriteFailure(exception: BleException?) {
                BleLog.w("command: read the current charging mode / charging current - fail")
                callback?.onReadFailure(exception)
            }
        }
        write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
    }

    /**
     * read the size of the battery log memory
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readBatteryLogMemory(bleDevice: BleDevice?,
                             callback: BleReadCallback?) {
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x80.toByte(), 0x00, 0x00)
        val writeCallback: BleWriteCallback = object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                BleLog.i("command: read the size of the battery log memory - success")
                if (callback != null) {
                    instance.read(bleDevice, callback)
                }
            }

            override fun onWriteFailure(exception: BleException?) {
                BleLog.w("command: read the size of the battery log memory - fail")
                callback?.onReadFailure(exception)
            }
        }
        write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
    }

    /**
     * read the number of battery data sets stored in the Flash/EEP memory
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readBatteryDataStored(bleDevice: BleDevice?,
                              callback: BleReadCallback?) {
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x82.toByte(), 0x00, 0x00)
        val writeCallback: BleWriteCallback = object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                BleLog.i("command: read the number of battery data sets stored in the Flash/EEP memory - success")
                if (callback != null) {
                    instance.read(bleDevice, callback)
                }
            }

            override fun onWriteFailure(exception: BleException?) {
                BleLog.w("command: read the number of battery data sets stored in the Flash/EEP memory - fail")
                callback?.onReadFailure(exception)
            }
        }
        write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
    }

    /**
     * read the battery data sets number (MSB, LSB)
     *
     * @param bleDevice
     * @param msb_lsb
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readBatteryDataSetsNumber(bleDevice: BleDevice?,
                                  msb_lsb: Int,
                                  callback: BleReadCallback?) {
        val msb = convertBinaryToDecimal((intToBinary(msb_lsb).substring(0,4)))
        val lsb = convertBinaryToDecimal((intToBinary(msb_lsb).substring(4,8)))
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x84.toByte(), msb.toByte(), lsb.toByte())
        val writeCallback: BleWriteCallback = object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                BleLog.i("command: read the battery data sets number (MSB, LSB) - success")
                if (callback != null) {
                    instance.read(bleDevice, callback)
                }
            }

            override fun onWriteFailure(exception: BleException?) {
                BleLog.w("command: read the battery data sets number (MSB, LSB) - fail")
                callback?.onReadFailure(exception)
            }
        }
        write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
    }

    /**
     * read the current battery data
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readCurrentBatteryData(bleDevice: BleDevice?,
                                  callback: BleReadCallback?) {
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x86.toByte(), 0x00, 0x00)
        val writeCallback: BleWriteCallback = object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                BleLog.i("command: read the current battery data - success")
                if (callback != null) {
                    instance.read(bleDevice, callback)
                }
            }

            override fun onWriteFailure(exception: BleException?) {
                BleLog.w("command: read the current battery data - fail")
                callback?.onReadFailure(exception)
            }
        }
        write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
    }

    /**
     * read the charger log data
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readChargerLogData(bleDevice: BleDevice?,
                           callback: BleReadCallback?) {
        val data = byteArrayOf(Sender_ID, Destination_ID, 0x04, 0x00, 0x90.toByte(), 0x00, 0x00)
        val writeCallback: BleWriteCallback = object : BleWriteCallback() {
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                BleLog.i("command: read the charger log data - success")
                if (callback != null) {
                    instance.read(bleDevice, callback)
                }
            }

            override fun onWriteFailure(exception: BleException?) {
                BleLog.w("command: read the charger log data - fail")
                callback?.onReadFailure(exception)
            }
        }
        write(bleDevice, Utils.dataWithChecksum(data), writeCallback)
    }

    /**
     * read
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun read(bleDevice: BleDevice?,
             callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector()
                    .withUUIDString(serviceUuid, characteristicUuid)
                    .readCharacteristic(callback, characteristicUuid,false)
        }
    }

    open fun uuidFromShortCode(shortCode: String): String {
        return "0000$shortCode-$baseBluetoothUuidPostfix"
    }


    /**
     * read manufacturer name
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readManufacturerName(bleDevice: BleDevice?,
                             callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            BleLog.i("read manufacturer name - success")
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuidFromShortCode(deviceInformationServiceUuid), uuidFromShortCode("2A29"))
                    .readCharacteristic(callback, uuidFromShortCode("2A29"),true)
        }
    }

    /**
     * read model number
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readModelNumber(bleDevice: BleDevice?,
                        callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            BleLog.i("read model number - success")
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuidFromShortCode(deviceInformationServiceUuid), uuidFromShortCode("2A24"))
                    .readCharacteristic(callback, uuidFromShortCode("2A24"),true)
        }
    }

    /**
     * read serial number
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readSerialNumber(bleDevice: BleDevice?,
                         callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            BleLog.i("read serial number - success")
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuidFromShortCode(deviceInformationServiceUuid), uuidFromShortCode("2A25"))
                    .readCharacteristic(callback, uuidFromShortCode("2A25"),true)
        }
    }

    /**
     * read hardware revision
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readHardwareRevision(bleDevice: BleDevice?,
                             callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            BleLog.i("read hardware revision - success")
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuidFromShortCode(deviceInformationServiceUuid), uuidFromShortCode("2A27"))
                    .readCharacteristic(callback, uuidFromShortCode("2A27"),true)
        }
    }

    /**
     * read firmware revision
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readFirmwareRevision(bleDevice: BleDevice?,
                             callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            BleLog.i("read firmware revision - success")
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuidFromShortCode(deviceInformationServiceUuid), uuidFromShortCode("2A26"))
                    .readCharacteristic(callback, uuidFromShortCode("2A26"),true)
        }
    }

    /**
     * read software revision
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readSoftwareRevision(bleDevice: BleDevice?,
                             callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            BleLog.i("read software revision - success")
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuidFromShortCode(deviceInformationServiceUuid), uuidFromShortCode("2A28"))
                    .readCharacteristic(callback, uuidFromShortCode("2A28"),true)
        }
    }

    /**
     * read system ID
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readSystemID(bleDevice: BleDevice?,
                             callback: BleReadCallback?) {
        requireNotNull(callback) { "BleReadCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onReadFailure(OtherException("This device is not connected!"))
        } else {
            BleLog.i("read system ID - success")
            bleBluetooth.newBleConnector()
                    .withUUIDString(uuidFromShortCode(deviceInformationServiceUuid), uuidFromShortCode("2A23"))
                    .readCharacteristic(callback, uuidFromShortCode("2A23"),true)
        }
    }

    /**
     * read Rssi
     *
     * @param bleDevice
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun readRssi(bleDevice: BleDevice?,
                 callback: BleRssiCallback?) {
        requireNotNull(callback) { "BleRssiCallback can not be Null!" }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onRssiFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector().readRemoteRssi(callback)
        }
    }

    /**
     * set Mtu
     *
     * @param bleDevice
     * @param mtu
     * @param callback
     */
    @ExperimentalUnsignedTypes
    fun setMtu(bleDevice: BleDevice?,
               mtu: Int,
               callback: BleMtuChangedCallback?) {
        requireNotNull(callback) { "BleMtuChangedCallback can not be Null!" }
        if (mtu > DEFAULT_MAX_MTU) {
            BleLog.e("requiredMtu should lower than 512 !")
            callback.onSetMTUFailure(OtherException("requiredMtu should lower than 512 !"))
            return
        }
        if (mtu < DEFAULT_MTU) {
            BleLog.e("requiredMtu should higher than 23 !")
            callback.onSetMTUFailure(OtherException("requiredMtu should higher than 23 !"))
            return
        }
        val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
        if (bleBluetooth == null) {
            callback.onSetMTUFailure(OtherException("This device is not connected!"))
        } else {
            bleBluetooth.newBleConnector().setMtu(mtu, callback)
        }
    }

    /**
     * requestConnectionPriority
     *
     * @param connectionPriority Request a specific connection priority. Must be one of
     * [BluetoothGatt.CONNECTION_PRIORITY_BALANCED],
     * [BluetoothGatt.CONNECTION_PRIORITY_HIGH]
     * or [BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER].
     * @throws IllegalArgumentException If the parameters are outside of their
     * specified range.
     */
    @ExperimentalUnsignedTypes
    fun requestConnectionPriority(bleDevice: BleDevice?, connectionPriority: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val bleBluetooth = multipleBluetoothController!!.getBleBluetooth(bleDevice)
            return bleBluetooth?.newBleConnector()?.requestConnectionPriority(connectionPriority)
                    ?: false
        }
        return false
    }

    /**
     * is support ble?
     *
     * @return
     */
    private val isSupportBle: Boolean
        get() = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context!!.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))

    /**
     * Open bluetooth
     */
    fun enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter!!.enable()
        }
    }

    /**
     * Disable bluetooth
     */
    fun disableBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter!!.isEnabled) {
            bluetoothAdapter!!.disable()
        }
    }

    /**
     * judge Bluetooth is enable
     *
     * @return
     */
    private val isBlueEnable: Boolean
        get() = bluetoothAdapter != null && bluetoothAdapter!!.isEnabled

    fun convertBleDevice(bluetoothDevice: BluetoothDevice?): BleDevice {
        return BleDevice(bluetoothDevice)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun convertBleDevice(scanResult: ScanResult?): BleDevice {
        requireNotNull(scanResult) { "scanResult can not be Null!" }
        val bluetoothDevice = scanResult.device
        val rssi = scanResult.rssi
        val scanRecord = scanResult.scanRecord
        var bytes: ByteArray? = null
        if (scanRecord != null) bytes = scanRecord.bytes
        val timestampNanos = scanResult.timestampNanos
        return BleDevice(bluetoothDevice, rssi, bytes, timestampNanos)
    }

    private fun getBleBluetooth(bleDevice: BleDevice?): BleBluetooth? {
        return if (multipleBluetoothController != null) {
            multipleBluetoothController!!.getBleBluetooth(bleDevice)
        } else null
    }

    private fun getBluetoothGatt(bleDevice: BleDevice?): BluetoothGatt? {
        val bleBluetooth = getBleBluetooth(bleDevice)
        return bleBluetooth?.bluetoothGatt
    }

    fun getBluetoothGattServices(bleDevice: BleDevice?): List<BluetoothGattService>? {
        val gatt = getBluetoothGatt(bleDevice)
        return gatt?.services
    }

    fun getBluetoothGattCharacteristics(service: BluetoothGattService): List<BluetoothGattCharacteristic> {
        return service.characteristics
    }

    fun removeConnectGattCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeConnectGattCallback()
    }

    fun removeRssiCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeRssiCallback()
    }

    fun removeMtuChangedCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeMtuChangedCallback()
    }

    fun removeWriteCallback(bleDevice: BleDevice?, uuidWrite: String?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeWriteCallback(uuidWrite!!)
    }

    fun removeReadCallback(bleDevice: BleDevice?, uuidRead: String?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.removeReadCallback(uuidRead!!)
    }

    fun clearCharacterCallback(bleDevice: BleDevice?) {
        val bleBluetooth = getBleBluetooth(bleDevice)
        bleBluetooth?.clearCharacterCallback()
    }

    val scanSate: BleScanState
        get() = BleScanner.instance.scanState

    val allConnectedDevice: List<BleDevice?>?
        get() = if (multipleBluetoothController == null) null else multipleBluetoothController!!.deviceList

    /**
     * @param bleDevice
     * @return State of the profile connection. One of
     * [BluetoothProfile.STATE_CONNECTED],
     * [BluetoothProfile.STATE_CONNECTING],
     * [BluetoothProfile.STATE_DISCONNECTED],
     * [BluetoothProfile.STATE_DISCONNECTING]
     */
    private fun getConnectState(bleDevice: BleDevice?): Int {
        return if (bleDevice != null) {
            bluetoothManager!!.getConnectionState(bleDevice.device, BluetoothProfile.GATT)
        } else {
            BluetoothProfile.STATE_DISCONNECTED
        }
    }

    fun isConnected(bleDevice: BleDevice?): Boolean {
        return getConnectState(bleDevice) == BluetoothProfile.STATE_CONNECTED
    }

    fun isConnected(mac: String): Boolean {
        val list = allConnectedDevice
        for (bleDevice in list!!) {
            if (bleDevice != null && bleDevice.mac == mac) {
                return true
            }
        }
        return false
    }

    fun disconnect(bleDevice: BleDevice?) {
        if (multipleBluetoothController != null) {
            multipleBluetoothController!!.disconnect(bleDevice)
        }
    }

    fun disconnectAllDevice() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController!!.disconnectAllDevice()
        }
    }

    fun destroy() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController!!.destroy()
        }
    }

    companion object {
        private const val DEFAULT_MAX_MULTIPLE_DEVICE = 7
        private const val DEFAULT_OPERATE_TIME = 5000
        private const val DEFAULT_CONNECT_RETRY_COUNT = 0
        private const val DEFAULT_CONNECT_RETRY_INTERVAL = 5000
        private const val DEFAULT_MTU = 23
        private const val DEFAULT_MAX_MTU = 512
        private const val DEFAULT_CONNECT_OVER_TIME = 10000
        private const val Sender_ID: Byte = 0x46
        private const val Destination_ID: Byte = 0x43
        private val serviceUuids = arrayOf(UUID.fromString("fc53a934-835b-0001-0000-7f438fd97a02"))
        private const val serviceUuid = "fc53a934-835b-0001-0000-7f438fd97a02"
        private const val deviceInformationServiceUuid = "180A"
        private const val characteristicUuid = "fc53a934-835b-0001-0001-7f438fd97a02"
        private const val baseBluetoothUuidPostfix = "0000-1000-8000-00805F9B34FB"

        @kotlin.jvm.JvmStatic
        val instance: BleManager
            get() = BleManagerHolder.sBleManager
    }
}