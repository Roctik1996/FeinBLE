package com.bbg.feinblelib;

import android.annotation.TargetApi;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;

import com.bbg.feinblelib.bluetooth.BleBluetooth;
import com.bbg.feinblelib.bluetooth.MultipleBluetoothController;
import com.bbg.feinblelib.callback.BleGattCallback;
import com.bbg.feinblelib.callback.BleMtuChangedCallback;
import com.bbg.feinblelib.callback.BleReadCallback;
import com.bbg.feinblelib.callback.BleRssiCallback;
import com.bbg.feinblelib.callback.BleScanCallback;
import com.bbg.feinblelib.callback.BleWriteCallback;
import com.bbg.feinblelib.data.BleDevice;
import com.bbg.feinblelib.data.BleScanState;
import com.bbg.feinblelib.exception.BleException;
import com.bbg.feinblelib.exception.OtherException;
import com.bbg.feinblelib.scan.BleScanner;
import com.bbg.feinblelib.utils.BleLog;

import java.util.List;
import java.util.UUID;

import static com.bbg.feinblelib.utils.Utils.dataWithChecksum;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleManager {

    private Application context;
    private BluetoothAdapter bluetoothAdapter;
    private MultipleBluetoothController multipleBluetoothController;
    private BluetoothManager bluetoothManager;

    private static final int DEFAULT_MAX_MULTIPLE_DEVICE = 7;
    private static final int DEFAULT_OPERATE_TIME = 5000;
    private static final int DEFAULT_CONNECT_RETRY_COUNT = 0;
    private static final int DEFAULT_CONNECT_RETRY_INTERVAL = 5000;
    private static final int DEFAULT_MTU = 23;
    private static final int DEFAULT_MAX_MTU = 512;
    private static final int DEFAULT_CONNECT_OVER_TIME = 10000;
    private static final byte Sender_ID = 0x46;
    private static final byte Destination_ID = 0x43;
    private static final UUID[] serviceUuids = new UUID[] {UUID.fromString("fc53a934-835b-0001-0000-7f438fd97a02")};
    private static final String serviceUuid = "fc53a934-835b-0001-0000-7f438fd97a02";
    private static final String characteristicUuid = "fc53a934-835b-0001-0001-7f438fd97a02";

    private int maxConnectCount = DEFAULT_MAX_MULTIPLE_DEVICE;
    private int operateTimeout = DEFAULT_OPERATE_TIME;
    private int reConnectCount = DEFAULT_CONNECT_RETRY_COUNT;
    private long reConnectInterval = DEFAULT_CONNECT_RETRY_INTERVAL;
    private long connectOverTime = DEFAULT_CONNECT_OVER_TIME;

    public static BleManager getInstance() {
        return BleManagerHolder.sBleManager;
    }

    private static class BleManagerHolder {
        private static final BleManager sBleManager = new BleManager();
    }

    public void init(Application app) {
        if (context == null && app != null) {
            context = app;
            if (isSupportBle()) {
                bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            }
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            multipleBluetoothController = new MultipleBluetoothController();
        }
    }

    /**
     * Get the Context
     *
     * @return
     */
    public Context getContext() {
        return context;
    }

    /**
     * Get the BluetoothManager
     *
     * @return
     */
    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    /**
     * Get the BluetoothAdapter
     *
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }


    /**
     * Get the multiple Bluetooth Controller
     *
     * @return
     */
    public MultipleBluetoothController getMultipleBluetoothController() {
        return multipleBluetoothController;
    }

    /**
     * Get the maximum number of connections
     *
     * @return
     */
    public int getMaxConnectCount() {
        return maxConnectCount;
    }

    /**
     * Set the maximum number of connections
     *
     * @param count
     * @return BleManager
     */
    public BleManager setMaxConnectCount(int count) {
        if (count > DEFAULT_MAX_MULTIPLE_DEVICE)
            count = DEFAULT_MAX_MULTIPLE_DEVICE;
        this.maxConnectCount = count;
        return this;
    }

    /**
     * Get operate timeout
     *
     * @return
     */
    public int getOperateTimeout() {
        return operateTimeout;
    }

    /**
     * Set operate timeout
     *
     * @param count
     * @return BleManager
     */
    public BleManager setOperateTimeout(int count) {
        this.operateTimeout = count;
        return this;
    }

    /**
     * Get connect retry count
     *
     * @return
     */
    public int getReConnectCount() {
        return reConnectCount;
    }

    /**
     * Get connect retry interval
     *
     * @return
     */
    public long getReConnectInterval() {
        return reConnectInterval;
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    public BleManager setReConnectCount(int count) {
        return setReConnectCount(count, DEFAULT_CONNECT_RETRY_INTERVAL);
    }

    /**
     * Set connect retry count and interval
     *
     * @param count
     * @return BleManager
     */
    public BleManager setReConnectCount(int count, long interval) {
        if (count > 10)
            count = 10;
        if (interval < 0)
            interval = 0;
        this.reConnectCount = count;
        this.reConnectInterval = interval;
        return this;
    }

    /**
     * Get operate connect Over Time
     *
     * @return
     */
    public long getConnectOverTime() {
        return connectOverTime;
    }

    /**
     * Set connect Over Time
     *
     * @param time
     * @return BleManager
     */
    public BleManager setConnectOverTime(long time) {
        if (time <= 0) {
            time = 100;
        }
        this.connectOverTime = time;
        return this;
    }

    /**
     * print log?
     *
     * @param enable
     * @return BleManager
     */
    public BleManager enableLog(boolean enable) {
        BleLog.isPrint = enable;
        return this;
    }

    /**
     * scan device around
     *
     * @param callback
     */
    public void scan(BleScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleScanCallback can not be Null!");
        }

        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!");
            callback.onScanStarted(false);
            return;
        }

        BleScanner.getInstance().scan(serviceUuids, connectOverTime, callback);
    }

    /**
     * connect a known device
     *
     * @param bleDevice
     * @param bleGattCallback
     * @return
     */
    public BluetoothGatt connect(BleDevice bleDevice, BleGattCallback bleGattCallback) {
        if (bleGattCallback == null) {
            throw new IllegalArgumentException("BleGattCallback can not be Null!");
        }

        if (!isBlueEnable()) {
            BleLog.e("Bluetooth not enable!");
            bleGattCallback.onConnectFail(bleDevice, new OtherException("Bluetooth not enable!"));
            return null;
        }

        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            BleLog.w("Be careful: currentThread is not MainThread!");
        }

        if (bleDevice == null || bleDevice.getDevice() == null) {
            bleGattCallback.onConnectFail(bleDevice, new OtherException("Not Found Device Exception Occurred!"));
        } else {
            BleBluetooth bleBluetooth = multipleBluetoothController.buildConnectingBle(bleDevice);
            return bleBluetooth.connect(bleDevice, bleGattCallback);
        }

        return null;
    }

    /**
     * connect a device through its mac without scan,whether or not it has been connected
     *
     * @param mac
     * @param bleGattCallback
     * @return
     */
    public BluetoothGatt connect(String mac, BleGattCallback bleGattCallback) {
        BluetoothDevice bluetoothDevice = getBluetoothAdapter().getRemoteDevice(mac);
        BleDevice bleDevice = new BleDevice(bluetoothDevice, 0, null, 0);
        return connect(bleDevice, bleGattCallback);
    }


    /**
     * Cancel scan
     */
    public void cancelScan() {
        BleScanner.getInstance().stopLeScan();
    }

    /**
     * write
     *
     * @param bleDevice
     * @param data
     * @param callback
     */
    public void write(BleDevice bleDevice,
                      byte[] data,
                      BleWriteCallback callback) {

        if (callback == null) {
            throw new IllegalArgumentException("BleWriteCallback can not be Null!");
        }

        if (data == null) {
            BleLog.e("data is Null!");
            callback.onWriteFailure(new OtherException("data is Null!"));
            return;
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onWriteFailure(new OtherException("This device not connect!"));
        } else {
                bleBluetooth.newBleConnector()
                        .withUUIDString(serviceUuid, characteristicUuid)
                        .writeCharacteristic(data, callback, characteristicUuid);
            }
    }


    /**
     * read current communication protocol version
     *
     * @param bleDevice
     * @param callback
     */
    public void readProtocolVersion(final BleDevice bleDevice,
                                    final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, 0x10, 0x00, 0x00};
        final BleWriteCallback writeCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                if (callback != null) {
                    BleManager.getInstance().read(bleDevice, callback);
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (callback != null) {
                    callback.onReadFailure(exception);
                }
            }
        };

        write(bleDevice, dataWithChecksum(data), writeCallback);
    }

    /**
     * set charging mode / charging current
     *
     * @param bleDevice
     * @param current - charging current in percent
     * @param callback
     */
    public void setChargingMode(final BleDevice bleDevice,
                                Integer current,
                                final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, 0x40, 0x00, current.byteValue()};

        if (current>=1&&current<=100) {
            BleWriteCallback writeCallback = new BleWriteCallback() {
                @Override
                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                    if (callback != null) {
                        BleManager.getInstance().read(bleDevice, callback);
                    }
                }

                @Override
                public void onWriteFailure(BleException exception) {
                    if (callback != null) {
                        callback.onReadFailure(exception);
                    }
                }
            };

            write(bleDevice, dataWithChecksum(data), writeCallback);
        }
        else{
            if (callback != null) {
                callback.onReadFailure(new OtherException("Current value must be >=1 and <=100"));
            }
        }
    }

    /**
     * read the current charging mode / charging current
     *
     * @param bleDevice
     * @param callback
     */
    public void readChargingMode(final BleDevice bleDevice,
                                 final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, 0x41, 0x00, 0x00};
        BleWriteCallback writeCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                if (callback != null) {
                    BleManager.getInstance().read(bleDevice, callback);
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (callback != null) {
                    callback.onReadFailure(exception);
                }
            }
        };

        write(bleDevice, dataWithChecksum(data), writeCallback);
    }

    /**
     * read the size of the battery log memory
     *
     * @param bleDevice
     * @param callback
     */
    public void readBatteryLogMemory(final BleDevice bleDevice,
                                     final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, (byte) 0x80, 0x00, 0x00};
        BleWriteCallback writeCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                if (callback != null) {
                    BleManager.getInstance().read(bleDevice, callback);
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (callback != null) {
                    callback.onReadFailure(exception);
                }
            }
        };

        write(bleDevice, dataWithChecksum(data), writeCallback);
    }

    /**
     * read the number of battery data sets stored in the Flash/EEP memory
     *
     * @param bleDevice
     * @param callback
     */
    public void readBatteryDataStored(final BleDevice bleDevice,
                                      final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, (byte) 0x82, 0x00, 0x00};
        BleWriteCallback writeCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                if (callback != null) {
                    BleManager.getInstance().read(bleDevice, callback);
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (callback != null) {
                    callback.onReadFailure(exception);
                }
            }
        };

        write(bleDevice, dataWithChecksum(data), writeCallback);
    }

    /**
     * read the battery data sets number (MSB, LSB)
     *
     * @param bleDevice
     * @param msb
     * @param lsb
     * @param callback
     */
    public void readBatteryDataSetsNumber(final BleDevice bleDevice,
                                          Integer msb,
                                          Integer lsb,
                                          final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, (byte) 0x84, msb.byteValue(), lsb.byteValue()};
        BleWriteCallback writeCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                if (callback != null) {
                    BleManager.getInstance().read(bleDevice, callback);
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (callback != null) {
                    callback.onReadFailure(exception);
                }
            }
        };

        write(bleDevice, dataWithChecksum(data), writeCallback);
    }

    /**
     * read the current battery data
     *
     * @param bleDevice
     * @param callback
     */
    public void readCurrentBatteryLogData(final BleDevice bleDevice,
                                          final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, (byte) 0x86, 0x00, 0x00};
        BleWriteCallback writeCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                if (callback != null) {
                    BleManager.getInstance().read(bleDevice, callback);
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (callback != null) {
                    callback.onReadFailure(exception);
                }
            }
        };

        write(bleDevice, dataWithChecksum(data), writeCallback);
    }

    /**
     * read the charger log data
     *
     * @param bleDevice
     * @param callback
     */
    public void readChargerLogData(final BleDevice bleDevice,
                                   final BleReadCallback callback) {
        byte[] data = new byte[]{Sender_ID, Destination_ID, 0x04, 0x00, (byte) 0x90, 0x00, 0x00};
        BleWriteCallback writeCallback = new BleWriteCallback() {
            @Override
            public void onWriteSuccess(int current, int total, byte[] justWrite) {
                if (callback != null) {
                    BleManager.getInstance().read(bleDevice, callback);
                }
            }

            @Override
            public void onWriteFailure(BleException exception) {
                if (callback != null) {
                    callback.onReadFailure(exception);
                }
            }
        };

        write(bleDevice, dataWithChecksum(data), writeCallback);
    }

    /**
     * read
     *
     * @param bleDevice
     * @param callback
     */
    public void read(BleDevice bleDevice,
                     BleReadCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleReadCallback can not be Null!");
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onReadFailure(new OtherException("This device is not connected!"));
        } else {
            bleBluetooth.newBleConnector()
                    .withUUIDString(serviceUuid, characteristicUuid)
                    .readCharacteristic(callback, characteristicUuid);
        }
    }

    /**
     * read Rssi
     *
     * @param bleDevice
     * @param callback
     */
    public void readRssi(BleDevice bleDevice,
                         BleRssiCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleRssiCallback can not be Null!");
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onRssiFailure(new OtherException("This device is not connected!"));
        } else {
            bleBluetooth.newBleConnector().readRemoteRssi(callback);
        }
    }

    /**
     * set Mtu
     *
     * @param bleDevice
     * @param mtu
     * @param callback
     */
    public void setMtu(BleDevice bleDevice,
                       int mtu,
                       BleMtuChangedCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleMtuChangedCallback can not be Null!");
        }

        if (mtu > DEFAULT_MAX_MTU) {
            BleLog.e("requiredMtu should lower than 512 !");
            callback.onSetMTUFailure(new OtherException("requiredMtu should lower than 512 !"));
            return;
        }

        if (mtu < DEFAULT_MTU) {
            BleLog.e("requiredMtu should higher than 23 !");
            callback.onSetMTUFailure(new OtherException("requiredMtu should higher than 23 !"));
            return;
        }

        BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
        if (bleBluetooth == null) {
            callback.onSetMTUFailure(new OtherException("This device is not connected!"));
        } else {
            bleBluetooth.newBleConnector().setMtu(mtu, callback);
        }
    }

    /**
     * requestConnectionPriority
     *
     * @param connectionPriority Request a specific connection priority. Must be one of
     *                           {@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED},
     *                           {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH}
     *                           or {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.
     * @throws IllegalArgumentException If the parameters are outside of their
     *                                  specified range.
     */
    public boolean requestConnectionPriority(BleDevice bleDevice, int connectionPriority) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BleBluetooth bleBluetooth = multipleBluetoothController.getBleBluetooth(bleDevice);
            if (bleBluetooth == null) {
                return false;
            } else {
                return bleBluetooth.newBleConnector().requestConnectionPriority(connectionPriority);
            }
        }
        return false;
    }

    /**
     * is support ble?
     *
     * @return
     */
    public boolean isSupportBle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && context.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Open bluetooth
     */
    public void enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    /**
     * Disable bluetooth
     */
    public void disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled())
                bluetoothAdapter.disable();
        }
    }

    /**
     * judge Bluetooth is enable
     *
     * @return
     */
    public boolean isBlueEnable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }


    public BleDevice convertBleDevice(BluetoothDevice bluetoothDevice) {
        return new BleDevice(bluetoothDevice);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BleDevice convertBleDevice(ScanResult scanResult) {
        if (scanResult == null) {
            throw new IllegalArgumentException("scanResult can not be Null!");
        }
        BluetoothDevice bluetoothDevice = scanResult.getDevice();
        int rssi = scanResult.getRssi();
        ScanRecord scanRecord = scanResult.getScanRecord();
        byte[] bytes = null;
        if (scanRecord != null)
            bytes = scanRecord.getBytes();
        long timestampNanos = scanResult.getTimestampNanos();
        return new BleDevice(bluetoothDevice, rssi, bytes, timestampNanos);
    }

    public BleBluetooth getBleBluetooth(BleDevice bleDevice) {
        if (multipleBluetoothController != null) {
            return multipleBluetoothController.getBleBluetooth(bleDevice);
        }
        return null;
    }

    public BluetoothGatt getBluetoothGatt(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            return bleBluetooth.getBluetoothGatt();
        return null;
    }

    public List<BluetoothGattService> getBluetoothGattServices(BleDevice bleDevice) {
        BluetoothGatt gatt = getBluetoothGatt(bleDevice);
        if (gatt != null) {
            return gatt.getServices();
        }
        return null;
    }

    public List<BluetoothGattCharacteristic> getBluetoothGattCharacteristics(BluetoothGattService service) {
        return service.getCharacteristics();
    }

    public void removeConnectGattCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeConnectGattCallback();
    }

    public void removeRssiCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeRssiCallback();
    }

    public void removeMtuChangedCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeMtuChangedCallback();
    }



    public void removeWriteCallback(BleDevice bleDevice, String uuid_write) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeWriteCallback(uuid_write);
    }

    public void removeReadCallback(BleDevice bleDevice, String uuid_read) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.removeReadCallback(uuid_read);
    }

    public void clearCharacterCallback(BleDevice bleDevice) {
        BleBluetooth bleBluetooth = getBleBluetooth(bleDevice);
        if (bleBluetooth != null)
            bleBluetooth.clearCharacterCallback();
    }

    public BleScanState getScanSate() {
        return BleScanner.getInstance().getScanState();
    }

    public List<BleDevice> getAllConnectedDevice() {
        if (multipleBluetoothController == null)
            return null;
        return multipleBluetoothController.getDeviceList();
    }

    /**
     * @param bleDevice
     * @return State of the profile connection. One of
     * {@link BluetoothProfile#STATE_CONNECTED},
     * {@link BluetoothProfile#STATE_CONNECTING},
     * {@link BluetoothProfile#STATE_DISCONNECTED},
     * {@link BluetoothProfile#STATE_DISCONNECTING}
     */
    public int getConnectState(BleDevice bleDevice) {
        if (bleDevice != null) {
            return bluetoothManager.getConnectionState(bleDevice.getDevice(), BluetoothProfile.GATT);
        } else {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
    }

    public boolean isConnected(BleDevice bleDevice) {
        return getConnectState(bleDevice) == BluetoothProfile.STATE_CONNECTED;
    }

    public boolean isConnected(String mac) {
        List<BleDevice> list = getAllConnectedDevice();
        for (BleDevice bleDevice : list) {
            if (bleDevice != null) {
                if (bleDevice.getMac().equals(mac)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void disconnect(BleDevice bleDevice) {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.disconnect(bleDevice);
        }
    }

    public void disconnectAllDevice() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.disconnectAllDevice();
        }
    }

    public void destroy() {
        if (multipleBluetoothController != null) {
            multipleBluetoothController.destroy();
        }
    }


}
