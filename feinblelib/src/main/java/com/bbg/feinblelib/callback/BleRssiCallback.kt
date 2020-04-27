package com.bbg.feinblelib.callback

import com.bbg.feinblelib.exception.BleException

abstract class BleRssiCallback : BleBaseCallback() {
    abstract fun onRssiFailure(exception: BleException?)
    abstract fun onRssiSuccess(rssi: Int)
}