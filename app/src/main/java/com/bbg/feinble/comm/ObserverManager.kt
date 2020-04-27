package com.bbg.feinble.comm

import com.bbg.feinblelib.data.BleDevice
import java.util.*

class ObserverManager : Observable {
    private object ObserverManagerHolder {
        val sObserverManager = ObserverManager()
    }

    private val observers: MutableList<Observer> = ArrayList()
    override fun addObserver(obj: Observer) {
        observers.add(obj)
    }

    override fun deleteObserver(obj: Observer) {
        val i = observers.indexOf(obj)
        if (i >= 0) {
            observers.remove(obj)
        }
    }

    override fun notifyObserver(bleDevice: BleDevice?) {
        for (i in observers.indices) {
            val o = observers[i]
            o.disConnected(bleDevice)
        }
    }

    companion object {
        val instance: ObserverManager
            get() = ObserverManagerHolder.sObserverManager
    }
}