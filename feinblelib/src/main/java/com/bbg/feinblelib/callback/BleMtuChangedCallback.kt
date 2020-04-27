package com.bbg.feinblelib.callback

import com.bbg.feinblelib.exception.BleException

abstract class BleMtuChangedCallback : BleBaseCallback() {
    abstract fun onSetMTUFailure(exception: BleException?)
    abstract fun onMtuChanged(mtu: Int)
}