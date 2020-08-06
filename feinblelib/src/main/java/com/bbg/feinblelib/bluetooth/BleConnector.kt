package com.bbg.feinblelib.bluetooth

import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.bbg.feinblelib.BleManager
import com.bbg.feinblelib.callback.BleMtuChangedCallback
import com.bbg.feinblelib.callback.BleReadCallback
import com.bbg.feinblelib.callback.BleRssiCallback
import com.bbg.feinblelib.callback.BleWriteCallback
import com.bbg.feinblelib.data.BleMsg
import com.bbg.feinblelib.data.BleWriteState
import com.bbg.feinblelib.exception.GattException
import com.bbg.feinblelib.exception.OtherException
import com.bbg.feinblelib.exception.TimeoutException
import com.bbg.feinblelib.utils.Parser
import java.util.*

@ExperimentalUnsignedTypes
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleConnector internal constructor(private val mBleBluetooth: BleBluetooth) {
    private val mBluetoothGatt: BluetoothGatt? = mBleBluetooth.bluetoothGatt
    private var mGattService: BluetoothGattService? = null
    private var mCharacteristic: BluetoothGattCharacteristic? = null
    private val mHandler: Handler
    private var isDeviceInfo=false
    private fun withUUID(serviceUUID: UUID?, characteristicUUID: UUID?): BleConnector {
        if (serviceUUID != null && mBluetoothGatt != null) {
            mGattService = mBluetoothGatt.getService(serviceUUID)
        }
        if (mGattService != null && characteristicUUID != null) {
            mCharacteristic = mGattService!!.getCharacteristic(characteristicUUID)
        }
        return this
    }

    fun withUUIDString(serviceUUID: String?, characteristicUUID: String?): BleConnector {
        return withUUID(formUUID(serviceUUID), formUUID(characteristicUUID))
    }

    private fun formUUID(uuid: String?): UUID? {
        return if (uuid == null) null else UUID.fromString(uuid)
    }
    /*------------------------------- main operation ----------------------------------- */
    /**
     * write
     */
    fun writeCharacteristic(data: ByteArray?, bleWriteCallback: BleWriteCallback?, uuidWrite: String) {
        if (data == null || data.isEmpty()) {
            bleWriteCallback?.onWriteFailure(OtherException("the data to be written is empty"))
            return
        }
        if (mCharacteristic == null
                || mCharacteristic!!.properties and (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
            bleWriteCallback?.onWriteFailure(OtherException("this characteristic not support write!"))
            return
        }
        if (mCharacteristic!!.setValue(data)) {
            handleCharacteristicWriteCallback(bleWriteCallback, uuidWrite)
            if (!mBluetoothGatt!!.writeCharacteristic(mCharacteristic)) {
                writeMsgInit()
                bleWriteCallback?.onWriteFailure(OtherException("gatt writeCharacteristic fail"))
            }
        } else {
            bleWriteCallback?.onWriteFailure(OtherException("Updates the locally stored value of this characteristic fail"))
        }
    }

    /**
     * read
     */
    fun readCharacteristic(bleReadCallback: BleReadCallback?, uuidRead: String,isDeviceInfo: Boolean) {
        if (mCharacteristic != null
                && mCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
            handleCharacteristicReadCallback(bleReadCallback, uuidRead,isDeviceInfo)
            if (!mBluetoothGatt!!.readCharacteristic(mCharacteristic)) {
                readMsgInit()
                bleReadCallback?.onReadFailure(OtherException("gatt readCharacteristic fail"))
            }
        } else {
            bleReadCallback?.onReadFailure(OtherException("this characteristic not support read!"))
        }
    }

    /**
     * rssi
     */
    fun readRemoteRssi(bleRssiCallback: BleRssiCallback?) {
        handleRSSIReadCallback(bleRssiCallback)
        if (!mBluetoothGatt!!.readRemoteRssi()) {
            rssiMsgInit()
            bleRssiCallback?.onRssiFailure(OtherException("gatt readRemoteRssi fail"))
        }
    }

    /**
     * set mtu
     */
    fun setMtu(requiredMtu: Int, bleMtuChangedCallback: BleMtuChangedCallback?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            handleSetMtuCallback(bleMtuChangedCallback)
            if (!mBluetoothGatt!!.requestMtu(requiredMtu)) {
                mtuChangedMsgInit()
                bleMtuChangedCallback?.onSetMTUFailure(OtherException("gatt requestMtu fail"))
            }
        } else {
            bleMtuChangedCallback?.onSetMTUFailure(OtherException("API level lower than 21"))
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
    fun requestConnectionPriority(connectionPriority: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothGatt!!.requestConnectionPriority(connectionPriority)
        } else false
    }
    /**************************************** Handle call back  */
    /**
     * write
     */
    private fun handleCharacteristicWriteCallback(bleWriteCallback: BleWriteCallback?,
                                                  uuidWrite: String) {
        if (bleWriteCallback != null) {
            writeMsgInit()
            bleWriteCallback.key = uuidWrite
            bleWriteCallback.handler = mHandler
            mBleBluetooth.addWriteCallback(uuidWrite, bleWriteCallback)
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_WRITE_START, bleWriteCallback),
                    BleManager.instance.operateTimeout.toLong())
        }
    }

    /**
     * read
     */
    private fun handleCharacteristicReadCallback(bleReadCallback: BleReadCallback?,
                                                 uuidRead: String,isDeviceInfo: Boolean) {
        if (bleReadCallback != null) {
            readMsgInit()
            bleReadCallback.key = uuidRead
            bleReadCallback.handler = mHandler
            this.isDeviceInfo = isDeviceInfo
            mBleBluetooth.addReadCallback(uuidRead, bleReadCallback)
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_READ_START, bleReadCallback),
                    BleManager.instance.operateTimeout.toLong())
        }
    }

    /**
     * rssi
     */
    private fun handleRSSIReadCallback(bleRssiCallback: BleRssiCallback?) {
        if (bleRssiCallback != null) {
            rssiMsgInit()
            bleRssiCallback.handler = mHandler
            mBleBluetooth.addRssiCallback(bleRssiCallback)
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_READ_RSSI_START, bleRssiCallback),
                    BleManager.instance.operateTimeout.toLong())
        }
    }

    /**
     * set mtu
     */
    private fun handleSetMtuCallback(bleMtuChangedCallback: BleMtuChangedCallback?) {
        if (bleMtuChangedCallback != null) {
            mtuChangedMsgInit()
            bleMtuChangedCallback.handler = mHandler
            mBleBluetooth.addMtuChangedCallback(bleMtuChangedCallback)
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_SET_MTU_START, bleMtuChangedCallback),
                    BleManager.instance.operateTimeout.toLong())
        }
    }

    fun writeMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_WRITE_START)
    }

    fun readMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_READ_START)
    }

    fun rssiMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_READ_RSSI_START)
    }

    fun mtuChangedMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_SET_MTU_START)
    }

    init {
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    BleMsg.MSG_CHA_WRITE_START -> {
                        val writeCallback = msg.obj as BleWriteCallback
                        writeCallback.onWriteFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_WRITE_RESULT -> {
                        writeMsgInit()
                        val writeCallback = msg.obj as BleWriteCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_WRITE_BUNDLE_STATUS)
                        val value = bundle.getByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            writeCallback.onWriteSuccess(BleWriteState.DATA_WRITE_SINGLE, BleWriteState.DATA_WRITE_SINGLE, value)
                        } else {
                            writeCallback.onWriteFailure(GattException(status))
                        }
                    }
                    BleMsg.MSG_CHA_READ_START -> {
                        val readCallback = msg.obj as BleReadCallback
                        readCallback.onReadFailure(TimeoutException())
                    }
                    BleMsg.MSG_CHA_READ_RESULT -> {
                        readMsgInit()
                        val readCallback = msg.obj as BleReadCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_READ_BUNDLE_STATUS)
                        val value = bundle.getByteArray(BleMsg.KEY_READ_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS && value != null) {
                            readCallback.onReadSuccess(Parser.parseCommand(value,isDeviceInfo))
                        } else {
                            readCallback.onReadFailure(GattException(status))
                        }
                    }
                    BleMsg.MSG_READ_RSSI_START -> {
                        val rssiCallback = msg.obj as BleRssiCallback
                        rssiCallback.onRssiFailure(TimeoutException())
                    }
                    BleMsg.MSG_READ_RSSI_RESULT -> {
                        rssiMsgInit()
                        val rssiCallback = msg.obj as BleRssiCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS)
                        val value = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            rssiCallback.onRssiSuccess(value)
                        } else {
                            rssiCallback.onRssiFailure(GattException(status))
                        }
                    }
                    BleMsg.MSG_SET_MTU_START -> {
                        val mtuChangedCallback = msg.obj as BleMtuChangedCallback
                        mtuChangedCallback.onSetMTUFailure(TimeoutException())
                    }
                    BleMsg.MSG_SET_MTU_RESULT -> {
                        mtuChangedMsgInit()
                        val mtuChangedCallback = msg.obj as BleMtuChangedCallback
                        val bundle = msg.data
                        val status = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS)
                        val value = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE)
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            mtuChangedCallback.onMtuChanged(value)
                        } else {
                            mtuChangedCallback.onSetMTUFailure(GattException(status))
                        }
                    }
                }
            }
        }
    }
}