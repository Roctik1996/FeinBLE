package com.bbg.feinblelib.callback

import com.bbg.feinblelib.exception.BleException

abstract class BleWriteCallback : BleBaseCallback() {
    abstract fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?)
    abstract fun onWriteFailure(exception: BleException?)
}