package com.bbg.feinblelib.callback

import com.bbg.feinblelib.data.BleDevice

abstract class BleScanCallback : BleScanPresenterImp {
    abstract fun onScanFinished(scanResultList: List<BleDevice>?)
    open fun onLeScan(bleDevice: BleDevice?) {}
}