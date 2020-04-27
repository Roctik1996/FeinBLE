package com.bbg.feinblelib.exception

class GattException(private var gattStatus: Int) : BleException(BleException.Companion.ERROR_CODE_GATT, "Gatt Exception Occurred! ") {

    fun setGattStatus(gattStatus: Int): GattException {
        this.gattStatus = gattStatus
        return this
    }

    override fun toString(): String {
        return "GattException{" +
                "gattStatus=" + gattStatus +
                "} " + super.toString()
    }

}