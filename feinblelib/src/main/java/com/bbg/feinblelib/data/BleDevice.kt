package com.bbg.feinblelib.data

import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

open class BleDevice : Parcelable {
    var device: BluetoothDevice?
    var scanRecord: ByteArray? = null
    var rssi = 0
    private var timestampNanos: Long = 0

    constructor(device: BluetoothDevice?) {
        this.device = device
    }

    constructor(device: BluetoothDevice?, rssi: Int, scanRecord: ByteArray?, timestampNanos: Long) {
        this.device = device
        this.scanRecord = scanRecord
        this.rssi = rssi
        this.timestampNanos = timestampNanos
    }

    protected constructor(parcel: Parcel) {
        device = parcel.readParcelable(BluetoothDevice::class.java.classLoader)
        scanRecord = parcel.createByteArray()
        rssi = parcel.readInt()
        timestampNanos = parcel.readLong()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(device, flags)
        dest.writeByteArray(scanRecord)
        dest.writeInt(rssi)
        dest.writeLong(timestampNanos)
    }

    override fun describeContents(): Int {
        return 0
    }

    val name: String?
        get() = if (device != null) device!!.name else null

    val mac: String?
        get() = if (device != null) device!!.address else null

    val key: String
        get() = if (device != null) device!!.name + device!!.address else ""

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BleDevice?> = object : Parcelable.Creator<BleDevice?> {
            override fun createFromParcel(parcel: Parcel): BleDevice? {
                return BleDevice(parcel)
            }

            override fun newArray(size: Int): Array<BleDevice?> {
                return arrayOfNulls(size)
            }
        }
    }
}