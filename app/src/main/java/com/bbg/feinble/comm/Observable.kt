package com.bbg.feinble.comm

import com.bbg.feinblelib.data.BleDevice

interface Observable {
    fun addObserver(obj: Observer)
    fun deleteObserver(obj: Observer)
    fun notifyObserver(bleDevice: BleDevice?)
}