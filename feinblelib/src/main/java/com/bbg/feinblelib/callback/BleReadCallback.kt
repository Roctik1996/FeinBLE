package com.bbg.feinblelib.callback

import com.bbg.feinblelib.exception.BleException
import java.util.*

abstract class BleReadCallback : BleBaseCallback() {
    abstract fun onReadSuccess(data: HashMap<*, *>?)
    abstract fun onReadFailure(exception: BleException?)
}