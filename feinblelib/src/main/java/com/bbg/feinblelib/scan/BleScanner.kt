package com.bbg.feinblelib.scan

import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.bbg.feinblelib.BleManager
import com.bbg.feinblelib.callback.BleScanAndConnectCallback
import com.bbg.feinblelib.callback.BleScanCallback
import com.bbg.feinblelib.callback.BleScanPresenterImp
import com.bbg.feinblelib.data.BleDevice
import com.bbg.feinblelib.data.BleScanState
import com.bbg.feinblelib.utils.BleLog
import java.util.*

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class BleScanner {
    private object BleScannerHolder {
        val sBleScanner = BleScanner()
    }

    var scanState = BleScanState.STATE_IDLE
        private set
    private val mBleScanPresenter: BleScanPresenter = object : BleScanPresenter() {
        override fun onScanStarted(success: Boolean) {
            val callback = this.bleScanPresenterImp
            callback?.onScanStarted(success)
        }

        override fun onLeScan(bleDevice: BleDevice?) {
            if (ismNeedConnect()) {
                val callback = this.bleScanPresenterImp as BleScanAndConnectCallback
                callback.onLeScan(bleDevice)
            } else {
                val callback = this.bleScanPresenterImp as BleScanCallback
                callback.onLeScan(bleDevice)
            }
        }

        override fun onScanning(bleDevice: BleDevice?) {
            val callback = this.bleScanPresenterImp
            callback?.onScanning(bleDevice)
        }

        override fun onScanFinished(bleDeviceList: List<BleDevice>?) {
            if (ismNeedConnect()) {
                val callback = this.bleScanPresenterImp as BleScanAndConnectCallback
                if (bleDeviceList == null || bleDeviceList.isEmpty()) {
                    callback.onScanFinished(null)
                } else {
                    callback.onScanFinished(bleDeviceList[0])
                    val list: List<BleDevice> = bleDeviceList
                    Handler(Looper.getMainLooper()).postDelayed({ BleManager.instance.connect(list[0], callback) }, 100)
                }
            } else {
                val callback = this.bleScanPresenterImp as BleScanCallback
                callback.onScanFinished(bleDeviceList)
            }
        }
    }

    fun scan(serviceUuid: Array<UUID>, timeOut: Long, callback: BleScanCallback?) {
        startLeScan(serviceUuid, timeOut, callback)
    }

    @Synchronized
    private fun startLeScan(serviceUuid: Array<UUID>, timeOut: Long, imp: BleScanPresenterImp?) {
        if (scanState != BleScanState.STATE_IDLE) {
            BleLog.w("scan action already exists, complete the previous scan action first")
            imp?.onScanStarted(false)
            return
        }
        mBleScanPresenter.prepare(timeOut, imp)
        val success: Boolean = BleManager.instance.bluetoothAdapter!!.startLeScan(serviceUuid, mBleScanPresenter)
        scanState = if (success) BleScanState.STATE_SCANNING else BleScanState.STATE_IDLE
        mBleScanPresenter.notifyScanStarted(success)
    }

    @Synchronized
    fun stopLeScan() {
        BleManager.instance.bluetoothAdapter?.stopLeScan(mBleScanPresenter)
        scanState = BleScanState.STATE_IDLE
        mBleScanPresenter.notifyScanStopped()
    }

    companion object {
        val instance: BleScanner
            get() = BleScannerHolder.sBleScanner
    }
}