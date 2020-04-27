package com.bbg.feinblelib.scan

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter.LeScanCallback
import android.bluetooth.BluetoothDevice
import android.os.*
import android.text.TextUtils
import com.bbg.feinblelib.callback.BleScanPresenterImp
import com.bbg.feinblelib.data.BleDevice
import com.bbg.feinblelib.data.BleMsg
import com.bbg.feinblelib.utils.BleLog
import com.bbg.feinblelib.utils.HexUtil.formatHexString
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
abstract class BleScanPresenter : LeScanCallback {
    private val mDeviceNames: Array<String>? = null
    private val mDeviceMac: String? = null
    private val mNeedConnect = false
    private var mScanTimeout: Long = 0
    var bleScanPresenterImp: BleScanPresenterImp? = null
        private set
    private val mBleDeviceList: MutableList<BleDevice> = ArrayList()
    private val mMainHandler = Handler(Looper.getMainLooper())
    private var mHandlerThread: HandlerThread? = null
    private var mHandler: Handler? = null
    private var mHandling = false

    private class ScanHandler internal constructor(looper: Looper?, bleScanPresenter: BleScanPresenter) : Handler(looper) {
        private val mBleScanPresenter: WeakReference<BleScanPresenter> = WeakReference(bleScanPresenter)
        override fun handleMessage(msg: Message) {
            val bleScanPresenter = mBleScanPresenter.get()
            if (bleScanPresenter != null && msg.what == BleMsg.MSG_SCAN_DEVICE) {
                val bleDevice = msg.obj as BleDevice
                bleScanPresenter.handleResult(bleDevice)
            }
        }

    }

    private fun handleResult(bleDevice: BleDevice) {
        mMainHandler.post { onLeScan(bleDevice) }
        checkDevice(bleDevice)
    }

    fun prepare(timeOut: Long, bleScanPresenterImp: BleScanPresenterImp?) {
        mScanTimeout = timeOut
        this.bleScanPresenterImp = bleScanPresenterImp
        mHandlerThread = HandlerThread(BleScanPresenter::class.java.simpleName)
        mHandlerThread!!.start()
        mHandler = ScanHandler(mHandlerThread!!.looper, this)
        mHandling = true
    }

    fun ismNeedConnect(): Boolean {
        return mNeedConnect
    }

    override fun onLeScan(device: BluetoothDevice, rssi: Int, scanRecord: ByteArray) {
        if (!mHandling) return
        val message = mHandler!!.obtainMessage()
        message.what = BleMsg.MSG_SCAN_DEVICE
        message.obj = BleDevice(device, rssi, scanRecord, System.currentTimeMillis())
        mHandler!!.sendMessage(message)
    }

    private fun checkDevice(bleDevice: BleDevice) {
        if (TextUtils.isEmpty(mDeviceMac) && (mDeviceNames == null || mDeviceNames.isEmpty())) {
            correctDeviceAndNextStep(bleDevice)
            return
        }
        if (!TextUtils.isEmpty(mDeviceMac) && !mDeviceMac.equals(bleDevice.mac, ignoreCase = true)) {
            return
        }
        if (mDeviceNames != null && mDeviceNames.isNotEmpty()) {
            val equal = AtomicBoolean(false)
            for (name in mDeviceNames) {
                var remoteName = bleDevice.name
                if (remoteName == null) remoteName = ""
                if (remoteName == name) {
                    equal.set(true)
                }
            }
            if (!equal.get()) {
                return
            }
        }
        correctDeviceAndNextStep(bleDevice)
    }

    private fun correctDeviceAndNextStep(bleDevice: BleDevice) {
        if (mNeedConnect) {
            BleLog.i("devices detected  ------"
                    + "  name:" + bleDevice.name
                    + "  mac:" + bleDevice.mac
                    + "  Rssi:" + bleDevice.rssi
                    + "  scanRecord:" + formatHexString(bleDevice.scanRecord))
            mBleDeviceList.add(bleDevice)
            mMainHandler.post { BleScanner.instance.stopLeScan() }
        } else {
            val hasFound = AtomicBoolean(false)
            for (result in mBleDeviceList) {
                if (result.device == bleDevice.device) {
                    hasFound.set(true)
                }
            }
            if (!hasFound.get()) {
                BleLog.i("device detected  ------"
                        + "  name: " + bleDevice.name
                        + "  mac: " + bleDevice.mac
                        + "  Rssi: " + bleDevice.rssi
                        + "  scanRecord: " + formatHexString(bleDevice.scanRecord, true))
                mBleDeviceList.add(bleDevice)
                mMainHandler.post { onScanning(bleDevice) }
            }
        }
    }

    fun notifyScanStarted(success: Boolean) {
        mBleDeviceList.clear()
        removeHandlerMsg()
        if (success && mScanTimeout > 0) {
            mMainHandler.postDelayed({ BleScanner.instance.stopLeScan() }, mScanTimeout)
        }
        mMainHandler.post { onScanStarted(success) }
    }

    fun notifyScanStopped() {
        mHandling = false
        mHandlerThread!!.quit()
        removeHandlerMsg()
        mMainHandler.post { onScanFinished(mBleDeviceList) }
    }

    private fun removeHandlerMsg() {
        mMainHandler.removeCallbacksAndMessages(null)
        mHandler!!.removeCallbacksAndMessages(null)
    }

    abstract fun onScanStarted(success: Boolean)
    abstract fun onLeScan(bleDevice: BleDevice?)
    abstract fun onScanning(bleDevice: BleDevice?)
    abstract fun onScanFinished(bleDeviceList: List<BleDevice>?)
}