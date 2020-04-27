package com.bbg.feinblelib.callback

import com.bbg.feinblelib.data.BleDevice

interface BleScanPresenterImp {
    fun onScanStarted(success: Boolean)
    fun onScanning(bleDevice: BleDevice?)
}