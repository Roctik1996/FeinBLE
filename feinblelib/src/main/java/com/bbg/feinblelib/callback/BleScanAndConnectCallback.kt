package com.bbg.feinblelib.callback

import com.bbg.feinblelib.data.BleDevice

abstract class BleScanAndConnectCallback : BleGattCallback(), BleScanPresenterImp {
    abstract fun onScanFinished(scanResult: BleDevice?)
    abstract fun onLeScan(bleDevice: BleDevice?)
}