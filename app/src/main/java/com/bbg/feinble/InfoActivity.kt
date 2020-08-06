package com.bbg.feinble

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bbg.feinble.comm.Observer
import com.bbg.feinble.comm.ObserverManager
import com.bbg.feinblelib.BleManager.Companion.instance
import com.bbg.feinblelib.callback.BleMtuChangedCallback
import com.bbg.feinblelib.callback.BleReadCallback
import com.bbg.feinblelib.data.BleDevice
import com.bbg.feinblelib.exception.BleException
import com.bbg.feinblelib.utils.BleLog
import com.bbg.feinblelib.utils.LogUtils
import java.util.*


class InfoActivity : AppCompatActivity(), Observer {
    private var bleDevice: BleDevice? = null

    @ExperimentalUnsignedTypes
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        val manufacturerBtn = findViewById<Button>(R.id.btn_manufacturer_name)
        val modelBtn = findViewById<Button>(R.id.btn_model_num)
        val serialBtn = findViewById<Button>(R.id.btn_serial_num)
        val hardwareBtn = findViewById<Button>(R.id.btn_hardware_num)
        val firmwareBtn = findViewById<Button>(R.id.btn_firmware_num)
        val softwareBtn = findViewById<Button>(R.id.btn_software_num)
        val systemBtn = findViewById<Button>(R.id.btn_system_id)
        val resultCommand = findViewById<TextView>(R.id.txt)
        bleDevice = intent.getParcelableExtra(KEY_DATA) as BleDevice?
        if (bleDevice == null) {
            finish()
        }

        instance.setMtu(bleDevice, 256, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException?) {
                BleLog.e(exception.toString())
            }

            override fun onMtuChanged(mtu: Int) {
                println(mtu)
            }
        })

        //Manufacturer Name String
        manufacturerBtn.setOnClickListener {
            instance.readManufacturerName(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "Manufacturer Name String" +
                                    "\nresponse: " + LogUtils.response +
                                    "\nparsed response: "
                            val sorted: TreeMap<Any?, Any?> = TreeMap(data)
                            val mappings: MutableSet<MutableMap.MutableEntry<Any?, Any?>> = sorted.entries
                            for ((key1, value) in mappings) resultCommand.text = resultCommand.text as String + "\n" + key1 + ":" + value
                            resultCommand.text = resultCommand.text as String + "\n___________________________\n"
                        }


                        override fun onReadFailure(exception: BleException?) {
                            BleLog.e(exception.toString())
                        }
                    })
        }


        //Model Number String
        modelBtn.setOnClickListener {
            instance.readModelNumber(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "Model Number String" +
                                    "\nresponse: " + LogUtils.response +
                                    "\nparsed response: "
                            val sorted: TreeMap<Any?, Any?> = TreeMap(data)
                            val mappings: MutableSet<MutableMap.MutableEntry<Any?, Any?>> = sorted.entries
                            for ((key1, value) in mappings) {
                                resultCommand.text = resultCommand.text as String + "\n" + key1 + ":" + value
                            }
                            resultCommand.text = resultCommand.text as String + "\n___________________________\n"
                        }

                        override fun onReadFailure(exception: BleException?) {
                            BleLog.e(exception.toString())
                        }
                    })
        }


        //Serial Number String
        serialBtn.setOnClickListener {
            instance.readSerialNumber(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "Serial Number String" +
                                    "\nresponse: " + LogUtils.response +
                                    "\nparsed response: "
                            val sorted: TreeMap<Any?, Any?> = TreeMap(data)
                            val mappings: MutableSet<MutableMap.MutableEntry<Any?, Any?>> = sorted.entries
                            for ((key1, value) in mappings) {
                                resultCommand.text = resultCommand.text as String + "\n" + key1 + ":" + value
                            }
                            resultCommand.text = resultCommand.text as String + "\n___________________________\n"
                        }

                        override fun onReadFailure(exception: BleException?) {
                            BleLog.e(exception.toString())
                        }
                    })
        }

        //Hardware Revision String
        hardwareBtn.setOnClickListener {
            instance.readHardwareRevision(
                    bleDevice, object : BleReadCallback() {
                override fun onReadSuccess(data: HashMap<*, *>?) {
                    resultCommand.text = resultCommand.text as String + "Hardware Revision String" +
                            "\nresponse: " + LogUtils.response +
                            "\nparsed response: "
                    val sorted: TreeMap<Any?, Any?> = TreeMap<Any?, Any?>(data)
                    val mappings: MutableSet<MutableMap.MutableEntry<Any?, Any?>> = sorted.entries
                    for ((key1, value) in mappings) {
                        resultCommand.text = resultCommand.text as String + "\n" + key1 + ":" + value
                    }
                    resultCommand.text = resultCommand.text as String + "\n___________________________\n"
                }

                override fun onReadFailure(exception: BleException?) {
                    BleLog.e(exception.toString())
                }
            })
        }

        //Firmware Revision String
        firmwareBtn.setOnClickListener {
            instance.readFirmwareRevision(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "Firmware Revision String" +
                                    "\nresponse: " + LogUtils.response +
                                    "\nparsed response: "
                            val sorted: TreeMap<Any?, Any?> = TreeMap(data)
                            val mappings: MutableSet<MutableMap.MutableEntry<Any?, Any?>> = sorted.entries
                            for ((key1, value) in mappings) {
                                resultCommand.text = resultCommand.text as String + "\n" + key1 + ":" + value
                            }
                            resultCommand.text = resultCommand.text as String + "\n___________________________\n"
                        }

                        override fun onReadFailure(exception: BleException?) {
                            BleLog.e(exception.toString())
                        }
                    })
        }


        //Software Revision String
        softwareBtn.setOnClickListener {
            instance.readSoftwareRevision(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "Software Revision String" +
                                    "\nresponse: " + LogUtils.response +
                                    "\nparsed response: "
                            val sorted: TreeMap<Any?, Any?> = TreeMap(data)
                            val mappings: MutableSet<MutableMap.MutableEntry<Any?, Any?>> = sorted.entries
                            for ((key1, value) in mappings) {
                                resultCommand.text = resultCommand.text as String + "\n" + key1 + ":" + value
                            }
                            resultCommand.text = resultCommand.text as String + "\n___________________________\n"
                        }

                        override fun onReadFailure(exception: BleException?) {
                            BleLog.e(exception.toString())
                        }
                    })
        }

        //System ID
        systemBtn.setOnClickListener {
            instance.readSystemID(
                    bleDevice, object : BleReadCallback() {
                override fun onReadSuccess(data: HashMap<*, *>?) {
                    resultCommand.text = resultCommand.text as String + "System ID" +
                            "\nresponse: " + LogUtils.response +
                            "\nparsed response: "
                    val sorted: TreeMap<Any?, Any?> = TreeMap(data)
                    val mappings: MutableSet<MutableMap.MutableEntry<Any?, Any?>> = sorted.entries
                    for ((key1, value) in mappings) {
                        resultCommand.text = resultCommand.text as String + "\n" + key1 + ":" + value
                    }
                    resultCommand.text = resultCommand.text as String + "\n___________________________\n"
                }

                override fun onReadFailure(exception: BleException?) {
                    BleLog.e(exception.toString())
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        instance.clearCharacterCallback(bleDevice)
        ObserverManager.instance.deleteObserver(this)
    }

    override fun disConnected(device: BleDevice?) {
        if (device != null && bleDevice != null && device.key == bleDevice!!.key) {
            finish()
        }
    }

    companion object {
        const val KEY_DATA = "key_data"
    }
}