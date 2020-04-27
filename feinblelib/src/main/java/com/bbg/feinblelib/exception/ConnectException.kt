package com.bbg.feinblelib.exception

import android.bluetooth.BluetoothGatt

class ConnectException(private var bluetoothGatt: BluetoothGatt, private var gattStatus: Int) : BleException(BleException.Companion.ERROR_CODE_GATT, "Gatt Exception Occurred! ") {

    fun setGattStatus(gattStatus: Int): ConnectException {
        this.gattStatus = gattStatus
        return this
    }

    fun setBluetoothGatt(bluetoothGatt: BluetoothGatt): ConnectException {
        this.bluetoothGatt = bluetoothGatt
        return this
    }

    override fun toString(): String {
        return "ConnectException{" +
                "gattStatus=" + gattStatus +
                ", bluetoothGatt=" + bluetoothGatt +
                "} " + super.toString()
    }

}