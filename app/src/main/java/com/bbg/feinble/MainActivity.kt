package com.bbg.feinble

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bbg.feinble.adapter.DeviceAdapter
import com.bbg.feinble.adapter.DeviceAdapter.OnDeviceClickListener
import com.bbg.feinble.comm.ObserverManager
import com.bbg.feinblelib.BleManager.Companion.instance
import com.bbg.feinblelib.callback.BleGattCallback
import com.bbg.feinblelib.callback.BleScanCallback
import com.bbg.feinblelib.data.BleDevice
import com.bbg.feinblelib.exception.BleException
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var btnScan: Button
    private var mDeviceAdapter: DeviceAdapter? = null
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        instance.init(application)
        instance
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000)
    }

    override fun onResume() {
        super.onResume()
        showConnectedDevice()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance.disconnectAllDevice()
        instance.destroy()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_scan) {
            if (btnScan.text == getString(R.string.start_scan)) {
                checkPermissions()
            } else if (btnScan.text == getString(R.string.stop_scan)) {
                instance.cancelScan()
            }
        }
    }

    private fun initView() {
        btnScan = findViewById(R.id.btn_scan)
        btnScan.text = (getString(R.string.start_scan))
        btnScan.setOnClickListener(this)

        progressDialog = ProgressDialog(this)
        mDeviceAdapter = DeviceAdapter(this)
        mDeviceAdapter!!.setOnDeviceClickListener(object : OnDeviceClickListener {
            override fun onConnect(bleDevice: BleDevice?) {
                if (!instance.isConnected(bleDevice)) {
                    instance.cancelScan()
                    connect(bleDevice)
                }
            }

            override fun onDisConnect(bleDevice: BleDevice?) {
                if (instance.isConnected(bleDevice)) {
                    instance.disconnect(bleDevice)
                }
            }

            override fun onDetail(bleDevice: BleDevice?) {
                if (instance.isConnected(bleDevice)) {
                    val intent = Intent(this@MainActivity, CommandActivity::class.java)
                    intent.putExtra(CommandActivity.KEY_DATA, bleDevice)
                    startActivity(intent)
                }
            }
        })
        val listViewDevice = findViewById<ListView>(R.id.list_device)
        listViewDevice.adapter = mDeviceAdapter
    }

    private fun showConnectedDevice() {
        val deviceList = instance.allConnectedDevice
        mDeviceAdapter!!.clearConnectedDevice()
        for (bleDevice in deviceList!!) {
            mDeviceAdapter!!.addDevice(bleDevice)
        }
        mDeviceAdapter!!.notifyDataSetChanged()
    }

    private fun startScan() {
        instance.scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                mDeviceAdapter!!.clearScanDevice()
                mDeviceAdapter!!.notifyDataSetChanged()
                btnScan.text = getString(R.string.stop_scan)
            }

            override fun onLeScan(bleDevice: BleDevice?) {
                super.onLeScan(bleDevice)
            }

            override fun onScanning(bleDevice: BleDevice?) {
                mDeviceAdapter!!.addDevice(bleDevice)
                mDeviceAdapter!!.notifyDataSetChanged()
            }

            override fun onScanFinished(scanResultList: List<BleDevice>?) {
                btnScan.text = getString(R.string.start_scan)
            }
        })
    }

    private fun connect(bleDevice: BleDevice?) {
        instance.connect(bleDevice, object : BleGattCallback() {
            override fun onStartConnect() {
                progressDialog!!.show()
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
                btnScan.text = getString(R.string.start_scan)
                progressDialog!!.dismiss()
                Toast.makeText(this@MainActivity, getString(R.string.connect_fail), Toast.LENGTH_LONG).show()
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                progressDialog!!.dismiss()
                mDeviceAdapter!!.addDevice(bleDevice)
                mDeviceAdapter!!.notifyDataSetChanged()
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
                progressDialog!!.dismiss()
                mDeviceAdapter!!.removeDevice(bleDevice)
                mDeviceAdapter!!.notifyDataSetChanged()
                if (isActiveDisConnected) {
                    Toast.makeText(this@MainActivity, getString(R.string.disconnected), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.disconnected), Toast.LENGTH_LONG).show()
                    ObserverManager.Companion.instance.notifyObserver(bleDevice)
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION_LOCATION && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted(permissions[i])
                }
            }
        }
    }

    private fun checkPermissions() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_LONG).show()
            return
        }
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionDeniedList: MutableList<String> = ArrayList()
        for (permission in permissions) {
            val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission)
            } else {
                permissionDeniedList.add(permission)
            }
        }
        if (permissionDeniedList.isNotEmpty()) {
            val deniedPermissions = permissionDeniedList.toTypedArray()
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION)
        }
    }

    private fun onPermissionGranted(permission: String) {
        if (Manifest.permission.ACCESS_FINE_LOCATION == permission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.notifyTitle)
                        .setMessage(R.string.gpsNotifyMsg)
                        .setNegativeButton(R.string.cancel
                        ) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                        .setPositiveButton(R.string.setting
                        ) { dialog: DialogInterface?, which: Int ->
                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS)
                        }
                        .setCancelable(false)
                        .show()
            } else {
                startScan()
            }
        }
    }

    private fun checkGPSIsOpen(): Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_OPEN_GPS && checkGPSIsOpen()) {
            startScan()
        }
    }

    companion object {
        private const val REQUEST_CODE_OPEN_GPS = 1
        private const val REQUEST_CODE_PERMISSION_LOCATION = 2
    }
}