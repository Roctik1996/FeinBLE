package com.bbg.feinble

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.widget.*
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


class CommandActivity : AppCompatActivity(), Observer {
    private var bleDevice: BleDevice? = null

    @ExperimentalUnsignedTypes
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_command)
        val protocolBtn = findViewById<Button>(R.id.btn_protocol)
        val setChargingBtn = findViewById<Button>(R.id.btn_set_charging_mode)
        val readChargingBtn = findViewById<Button>(R.id.btn_read_charging_mode)
        val logMemoryBtn = findViewById<Button>(R.id.btn_log_memory)
        val flashBtn = findViewById<Button>(R.id.btn_flash)
        val setsBtn = findViewById<Button>(R.id.btn_sets)
        val batteryDataBtn = findViewById<Button>(R.id.btn_battery_data)
        val chargerLogBtn = findViewById<Button>(R.id.btn_charger_log_data)
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

        //read current communication protocol version
        protocolBtn.setOnClickListener {
            instance.readProtocolVersion(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "read current communication protocol version" +
                                    "\ncommand: " + LogUtils.getCommand() +
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


        //set charging mode / charging current
        setChargingBtn.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Charging")
            alertDialog.setMessage("Enter charging current")
            val input = EditText(this)
            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            input.inputType = InputType.TYPE_CLASS_NUMBER
            alertDialog.setView(input)
            alertDialog.setPositiveButton("Set"
            ) { dialog: DialogInterface?, which: Int ->
                if (input.text.isNotEmpty()) {
                    if (input.text.toString().toInt() in 1..100) {
                        instance.setChargingMode(
                                bleDevice,
                                Integer.valueOf(input.text.toString()),
                                object : BleReadCallback() {
                                    override fun onReadSuccess(data: HashMap<*, *>?) {
                                        resultCommand.text = resultCommand.text as String + "set charging mode / charging current" +
                                                "\ncommand: " + LogUtils.getCommand() +
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
                    } else Toast.makeText(this, "Min. value is 1, highest value is 100", Toast.LENGTH_LONG).show()
                } else
                    Toast.makeText(this, "field is empty", Toast.LENGTH_LONG).show()
            }
            alertDialog.setNegativeButton("Cancel"
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            alertDialog.show()
        }


        //read the current charging mode / charging current
        readChargingBtn.setOnClickListener {
            instance.readChargingMode(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "read the current charging mode / charging current" +
                                    "\ncommand: " + LogUtils.getCommand() +
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

        //read the size of the battery log memory
        logMemoryBtn.setOnClickListener {
            instance.readBatteryLogMemory(
                    bleDevice, object : BleReadCallback() {
                override fun onReadSuccess(data: HashMap<*, *>?) {
                    resultCommand.text = resultCommand.text as String + "read the size of the battery log memory" +
                            "\ncommand: " + LogUtils.getCommand() +
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

        //read the number of battery data sets stored in the Flash/EEP memory
        flashBtn.setOnClickListener {
            instance.readBatteryDataStored(
                    bleDevice,
                    object : BleReadCallback() {
                        override fun onReadSuccess(data: HashMap<*, *>?) {
                            resultCommand.text = resultCommand.text as String + "read the number of battery data sets stored in the Flash/EEP memory" +
                                    "\ncommand: " + LogUtils.getCommand() +
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


        //read the battery data sets number (MSB, LSB)
        setsBtn.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("read the battery data sets number")
            val msb = EditText(this)
            val linearLayout = LinearLayout(alertDialog.context)
            linearLayout.orientation = LinearLayout.VERTICAL

            msb.inputType = InputType.TYPE_CLASS_NUMBER
            linearLayout.addView(msb)

            alertDialog.setView(linearLayout)

            alertDialog.setPositiveButton("Set"
            ) { dialog: DialogInterface?, which: Int ->
                if (msb.text.isNotEmpty()) {
                    if (msb.text.toString().toInt() in 1..65535) {
                        instance.readBatteryDataSetsNumber(
                                bleDevice,
                                Integer.parseInt(msb.text.toString()),
                                object : BleReadCallback() {
                                    override fun onReadSuccess(data: HashMap<*, *>?) {
                                        resultCommand.text = resultCommand.text as String + "read the battery data sets number (MSB, LSB)" +
                                                "\ncommand: " + LogUtils.getCommand() +
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
                    } else
                        Toast.makeText(this, "Min. value is 0, highest value is 65535", Toast.LENGTH_LONG).show()
                } else
                    Toast.makeText(alertDialog.context, "field is empty", Toast.LENGTH_LONG).show()
            }
            alertDialog.setNegativeButton("Cancel"
            ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            alertDialog.show()
        }

        //read the current battery data
        batteryDataBtn.setOnClickListener {
            instance.readCurrentBatteryData(
                    bleDevice, object : BleReadCallback() {
                override fun onReadSuccess(data: HashMap<*, *>?) {
                    resultCommand.text = resultCommand.text as String + "read the current battery data" +
                            "\ncommand: " + LogUtils.getCommand() +
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

        //read the charger log data
        chargerLogBtn.setOnClickListener {
            instance.readChargerLogData(
                    bleDevice, object : BleReadCallback() {
                override fun onReadSuccess(data: HashMap<*, *>?) {
                    resultCommand.text = resultCommand.text as String + "read the charger log data" +
                            "\ncommand: " + LogUtils.getCommand() +
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