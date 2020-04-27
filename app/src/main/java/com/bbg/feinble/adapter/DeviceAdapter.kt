package com.bbg.feinble.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bbg.feinble.R
import com.bbg.feinblelib.BleManager.Companion.instance
import com.bbg.feinblelib.data.BleDevice
import java.util.*

class DeviceAdapter(private val context: Context) : BaseAdapter() {

    private val bleDeviceList: MutableList<BleDevice>
    fun addDevice(bleDevice: BleDevice?) {
        removeDevice(bleDevice)
        bleDevice?.let { bleDeviceList.add(it) }
    }

    fun removeDevice(bleDevice: BleDevice?) {
        for (i in bleDeviceList.indices) {
            val device = bleDeviceList[i]
            if (bleDevice!!.key == device.key) {
                bleDeviceList.removeAt(i)
            }
        }
    }

    fun clearConnectedDevice() {
        for (i in bleDeviceList.indices) {
            val device = bleDeviceList[i]
            if (instance.isConnected(device)) {
                bleDeviceList.removeAt(i)
            }
        }
    }

    fun clearScanDevice() {
        for (i in bleDeviceList.indices) {
            val device = bleDeviceList[i]
            if (!instance.isConnected(device)) {
                bleDeviceList.removeAt(i)
            }
        }
    }

    fun clear() {
        clearConnectedDevice()
        clearScanDevice()
    }

    override fun getCount(): Int {
        return bleDeviceList.size
    }

    override fun getItem(position: Int): BleDevice {
        return (if (position > bleDeviceList.size) null else bleDeviceList[position])!!
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView != null) {
            holder = convertView.tag as ViewHolder
        } else {
            convertView = View.inflate(context, R.layout.adapter_device, null)
            holder = ViewHolder()
            convertView.tag = holder
            holder.imgBlue = convertView.findViewById(R.id.img_blue)
            holder.txtName = convertView.findViewById(R.id.txt_name)
            holder.txtMac = convertView.findViewById(R.id.txt_mac)
            holder.txtRssi = convertView.findViewById(R.id.txt_rssi)
            holder.layoutIdle = convertView.findViewById(R.id.layout_idle)
            holder.layoutConnected = convertView.findViewById(R.id.layout_connected)
            holder.btnDisconnect = convertView.findViewById(R.id.btn_disconnect)
            holder.btnConnect = convertView.findViewById(R.id.btn_connect)
            holder.btnDetail = convertView.findViewById(R.id.btn_detail)
        }
        val bleDevice = getItem(position)
        val isConnected = instance.isConnected(bleDevice)
        val name = bleDevice.name
        val mac = bleDevice.mac
        val rssi = bleDevice.rssi
        holder.txtName!!.text = name
        holder.txtMac!!.text = mac
        holder.txtRssi!!.text = rssi.toString()
        if (isConnected) {
            holder.imgBlue!!.setImageResource(R.drawable.ic_blue_connected)
            holder.txtName!!.setTextColor(-0xe2164a)
            holder.txtMac!!.setTextColor(-0xe2164a)
            holder.layoutIdle!!.visibility = View.GONE
            holder.layoutConnected!!.visibility = View.VISIBLE
        } else {
            holder.imgBlue!!.setImageResource(R.drawable.ic_blue_remote)
            holder.txtName!!.setTextColor(-0x1000000)
            holder.txtMac!!.setTextColor(-0x1000000)
            holder.layoutIdle!!.visibility = View.VISIBLE
            holder.layoutConnected!!.visibility = View.GONE
        }
        holder.btnConnect!!.setOnClickListener {
            if (mListener != null) {
                mListener!!.onConnect(bleDevice)
            }
        }
        holder.btnDisconnect!!.setOnClickListener {
            if (mListener != null) {
                mListener!!.onDisConnect(bleDevice)
            }
        }
        holder.btnDetail!!.setOnClickListener {
            if (mListener != null) {
                mListener!!.onDetail(bleDevice)
            }
        }
        return convertView!!
    }

    internal inner class ViewHolder {
        var imgBlue: ImageView? = null
        var txtName: TextView? = null
        var txtMac: TextView? = null
        var txtRssi: TextView? = null
        var layoutIdle: LinearLayout? = null
        var layoutConnected: LinearLayout? = null
        var btnDisconnect: Button? = null
        var btnConnect: Button? = null
        var btnDetail: Button? = null
    }

    interface OnDeviceClickListener {
        fun onConnect(bleDevice: BleDevice?)
        fun onDisConnect(bleDevice: BleDevice?)
        fun onDetail(bleDevice: BleDevice?)
    }

    private var mListener: OnDeviceClickListener? = null
    fun setOnDeviceClickListener(listener: OnDeviceClickListener?) {
        mListener = listener
    }

    init {
        bleDeviceList = ArrayList()
    }
}