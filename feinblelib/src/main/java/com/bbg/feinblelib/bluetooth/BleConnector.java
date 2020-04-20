
package com.bbg.feinblelib.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.bbg.feinblelib.BleManager;
import com.bbg.feinblelib.callback.BleMtuChangedCallback;
import com.bbg.feinblelib.callback.BleReadCallback;
import com.bbg.feinblelib.callback.BleRssiCallback;
import com.bbg.feinblelib.callback.BleWriteCallback;
import com.bbg.feinblelib.data.BleMsg;
import com.bbg.feinblelib.data.BleWriteState;
import com.bbg.feinblelib.exception.GattException;
import com.bbg.feinblelib.exception.OtherException;
import com.bbg.feinblelib.exception.TimeoutException;

import java.util.UUID;

import static com.bbg.feinblelib.utils.Parser.parseCommand;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleConnector {

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mGattService;
    private BluetoothGattCharacteristic mCharacteristic;
    private BleBluetooth mBleBluetooth;
    private Handler mHandler;

    BleConnector(BleBluetooth bleBluetooth) {
        this.mBleBluetooth = bleBluetooth;
        this.mBluetoothGatt = bleBluetooth.getBluetoothGatt();
        this.mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {

                    case BleMsg.MSG_CHA_WRITE_START: {
                        BleWriteCallback writeCallback = (BleWriteCallback) msg.obj;
                        if (writeCallback != null) {
                            writeCallback.onWriteFailure(new TimeoutException());
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_WRITE_RESULT: {
                        writeMsgInit();

                        BleWriteCallback writeCallback = (BleWriteCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_WRITE_BUNDLE_STATUS);
                        byte[] value = bundle.getByteArray(BleMsg.KEY_WRITE_BUNDLE_VALUE);
                        if (writeCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                writeCallback.onWriteSuccess(BleWriteState.DATA_WRITE_SINGLE, BleWriteState.DATA_WRITE_SINGLE, value);
                            } else {
                                writeCallback.onWriteFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_CHA_READ_START: {
                        BleReadCallback readCallback = (BleReadCallback) msg.obj;
                        if (readCallback != null)
                            readCallback.onReadFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_CHA_READ_RESULT: {
                        readMsgInit();

                        BleReadCallback readCallback = (BleReadCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_READ_BUNDLE_STATUS);
                        byte[] value = bundle.getByteArray(BleMsg.KEY_READ_BUNDLE_VALUE);
                        if (readCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS && value != null) {
                                readCallback.onReadSuccess(parseCommand(value));
                            } else {
                                readCallback.onReadFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_READ_RSSI_START: {
                        BleRssiCallback rssiCallback = (BleRssiCallback) msg.obj;
                        if (rssiCallback != null)
                            rssiCallback.onRssiFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_READ_RSSI_RESULT: {
                        rssiMsgInit();

                        BleRssiCallback rssiCallback = (BleRssiCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_STATUS);
                        int value = bundle.getInt(BleMsg.KEY_READ_RSSI_BUNDLE_VALUE);
                        if (rssiCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                rssiCallback.onRssiSuccess(value);
                            } else {
                                rssiCallback.onRssiFailure(new GattException(status));
                            }
                        }
                        break;
                    }

                    case BleMsg.MSG_SET_MTU_START: {
                        BleMtuChangedCallback mtuChangedCallback = (BleMtuChangedCallback) msg.obj;
                        if (mtuChangedCallback != null)
                            mtuChangedCallback.onSetMTUFailure(new TimeoutException());
                        break;
                    }

                    case BleMsg.MSG_SET_MTU_RESULT: {
                        mtuChangedMsgInit();

                        BleMtuChangedCallback mtuChangedCallback = (BleMtuChangedCallback) msg.obj;
                        Bundle bundle = msg.getData();
                        int status = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_STATUS);
                        int value = bundle.getInt(BleMsg.KEY_SET_MTU_BUNDLE_VALUE);
                        if (mtuChangedCallback != null) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                mtuChangedCallback.onMtuChanged(value);
                            } else {
                                mtuChangedCallback.onSetMTUFailure(new GattException(status));
                            }
                        }
                        break;
                    }
                }
            }
        };

    }

    private BleConnector withUUID(UUID serviceUUID, UUID characteristicUUID) {
        if (serviceUUID != null && mBluetoothGatt != null) {
            mGattService = mBluetoothGatt.getService(serviceUUID);
        }
        if (mGattService != null && characteristicUUID != null) {
            mCharacteristic = mGattService.getCharacteristic(characteristicUUID);
        }
        return this;
    }

    public BleConnector withUUIDString(String serviceUUID, String characteristicUUID) {
        return withUUID(formUUID(serviceUUID), formUUID(characteristicUUID));
    }

    private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }


    /*------------------------------- main operation ----------------------------------- */

    /**
     * write
     */
    public void writeCharacteristic(byte[] data, BleWriteCallback bleWriteCallback, String uuid_write) {
        if (data == null || data.length <= 0) {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("the data to be written is empty"));
            return;
        }

        if (mCharacteristic == null
                || (mCharacteristic.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0) {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("this characteristic not support write!"));
            return;
        }

        if (mCharacteristic.setValue(data)) {
            handleCharacteristicWriteCallback(bleWriteCallback, uuid_write);
            if (!mBluetoothGatt.writeCharacteristic(mCharacteristic)) {
                writeMsgInit();
                if (bleWriteCallback != null)
                    bleWriteCallback.onWriteFailure(new OtherException("gatt writeCharacteristic fail"));
            }
        } else {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("Updates the locally stored value of this characteristic fail"));
        }
    }

    /**
     * read
     */
    public void readCharacteristic(BleReadCallback bleReadCallback, String uuid_read) {
        if (mCharacteristic != null
                && (mCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {

            handleCharacteristicReadCallback(bleReadCallback, uuid_read);
            if (!mBluetoothGatt.readCharacteristic(mCharacteristic)) {
                readMsgInit();
                if (bleReadCallback != null)
                    bleReadCallback.onReadFailure(new OtherException("gatt readCharacteristic fail"));
            }
        } else {
            if (bleReadCallback != null)
                bleReadCallback.onReadFailure(new OtherException("this characteristic not support read!"));
        }
    }

    /**
     * rssi
     */
    public void readRemoteRssi(BleRssiCallback bleRssiCallback) {
        handleRSSIReadCallback(bleRssiCallback);
        if (!mBluetoothGatt.readRemoteRssi()) {
            rssiMsgInit();
            if (bleRssiCallback != null)
                bleRssiCallback.onRssiFailure(new OtherException("gatt readRemoteRssi fail"));
        }
    }

    /**
     * set mtu
     */
    public void setMtu(int requiredMtu, BleMtuChangedCallback bleMtuChangedCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            handleSetMtuCallback(bleMtuChangedCallback);
            if (!mBluetoothGatt.requestMtu(requiredMtu)) {
                mtuChangedMsgInit();
                if (bleMtuChangedCallback != null)
                    bleMtuChangedCallback.onSetMTUFailure(new OtherException("gatt requestMtu fail"));
            }
        } else {
            if (bleMtuChangedCallback != null)
                bleMtuChangedCallback.onSetMTUFailure(new OtherException("API level lower than 21"));
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
    public boolean requestConnectionPriority(int connectionPriority) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return mBluetoothGatt.requestConnectionPriority(connectionPriority);
        }
        return false;
    }


    /**************************************** Handle call back ******************************************/

    /**
     * write
     */
    private void handleCharacteristicWriteCallback(BleWriteCallback bleWriteCallback,
                                                   String uuid_write) {
        if (bleWriteCallback != null) {
            writeMsgInit();
            bleWriteCallback.setKey(uuid_write);
            bleWriteCallback.setHandler(mHandler);
            mBleBluetooth.addWriteCallback(uuid_write, bleWriteCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_WRITE_START, bleWriteCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * read
     */
    private void handleCharacteristicReadCallback(BleReadCallback bleReadCallback,
                                                  String uuid_read) {
        if (bleReadCallback != null) {
            readMsgInit();
            bleReadCallback.setKey(uuid_read);
            bleReadCallback.setHandler(mHandler);
            mBleBluetooth.addReadCallback(uuid_read, bleReadCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_CHA_READ_START, bleReadCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * rssi
     */
    private void handleRSSIReadCallback(BleRssiCallback bleRssiCallback) {
        if (bleRssiCallback != null) {
            rssiMsgInit();
            bleRssiCallback.setHandler(mHandler);
            mBleBluetooth.addRssiCallback(bleRssiCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_READ_RSSI_START, bleRssiCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    /**
     * set mtu
     */
    private void handleSetMtuCallback(BleMtuChangedCallback bleMtuChangedCallback) {
        if (bleMtuChangedCallback != null) {
            mtuChangedMsgInit();
            bleMtuChangedCallback.setHandler(mHandler);
            mBleBluetooth.addMtuChangedCallback(bleMtuChangedCallback);
            mHandler.sendMessageDelayed(
                    mHandler.obtainMessage(BleMsg.MSG_SET_MTU_START, bleMtuChangedCallback),
                    BleManager.getInstance().getOperateTimeout());
        }
    }

    public void writeMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_WRITE_START);
    }

    public void readMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_CHA_READ_START);
    }

    public void rssiMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_READ_RSSI_START);
    }

    public void mtuChangedMsgInit() {
        mHandler.removeMessages(BleMsg.MSG_SET_MTU_START);
    }

}
