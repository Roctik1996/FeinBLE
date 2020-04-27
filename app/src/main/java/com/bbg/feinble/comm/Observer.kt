package com.bbg.feinble.comm

import com.bbg.feinblelib.data.BleDevice

interface Observer {
    fun disConnected(bleDevice: BleDevice?)
}