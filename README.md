# Fein BLE

Android Bluetooth Low Energy

- Filtering, scanning, reading, writing and cancellation in a simple way.
- Support multi device connections  
- Support reconnection  
- Support configuration timeout for conncet or operation 

# Usage

- #### Init
    
        BleManager.getInstance().init(getApplication());

- #### Determine whether the current Android system supports BLE

        boolean isSupportBle()

- #### Open or close Bluetooth

		void enableBluetooth()
		void disableBluetooth()

- #### Initialization configuration

        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
	            .setSplitWriteNum(20)
	            .setConnectOverTime(10000)
                .setOperateTimeout(5000);=

- #### Scan

	`void scan(BleScanCallback callback)`

        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

            }

            @Override
            public void onScanning(BleDevice bleDevice) {

            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {

            }
        });

	Tips:
	- The scanning and filtering process is carried out in the worker thread, so it will not affect the UI operation of the main thread. Eventually, every callback result will return to the main thread.。


- #### Connect with device


	`BluetoothGatt connect(BleDevice bleDevice, BleGattCallback bleGattCallback)`

        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }
        });

	Tips:
	- On some types of phones, connectGatt must be effective on the main thread. It is very recommended that the connection process be placed in the main thread.
	- After connection failure, reconnect: the framework contains reconnection mechanism after connection failure, which can configure reconnection times and intervals. Of course, you can also call the `connect` method in `onConnectFail` callback automatically.
	- The connection is disconnected and reconnected: you can call the `connect` method again in the `onDisConnected` callback method.
	- In order to ensure the success rate of reconnection, it is recommended to reconnect after a period of interval.
	- When some models fail, they will be unable to scan devices for a short time. They can be connected directly through device objects or devices MAC without scanning.

- #### Connect with Mac

	`BluetoothGatt connect(String mac, BleGattCallback bleGattCallback)`

        BleManager.getInstance().connect(mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {

            }
        });

	Tips:
	- This method can attempt to connect directly to the BLE device around the Mac without scanning.
	- In many usage scenarios, I suggest that APP save the Mac of the user's customary device, then use this method to connect, which will greatly improve the connection efficiency.




- #### Cancel scan

	`void cancelScan()`

		BleManager.getInstance().cancelScan();

	Tips:
	- If this method is called, if it is still in the scan state, it will end immediately, and callback the `onScanFinished` method.



- #### Write

	`void write(BleDevice bleDevice, byte[] data, BleWriteCallback callback)`
	
		void write(BleDevice bleDevice, byte[] data, BleWriteCallback callback){
			@Override
			public void onWriteSuccess(int current, int total, byte[] justWrite) {

			}

			@Override
			public void onWriteFailure(BleException exception) {

			}
		});

	
- #### Read

	`void read(BleDevice bleDevice, BleReadCallback callback)`
	
		void read(BleDevice bleDevice, BleReadCallback callback){
            @Override
            public void onReadSuccess(byte[] data) {

            }

            @Override
            public void onReadFailure(BleException exception) {

            }
        });
				
				
- #### Command: Read current communication protocol version	

	`BleManager.getInstance().readProtocolVersion(bleDevice, new BleReadCallback())`
	
		BleManager.getInstance().readProtocolVersion(bleDevice, new BleReadCallback() {
            @Override
            public void onReadSuccess(HashMap data) {
                       
            }

            @Override
            public void onReadFailure(BleException exception) {

            }
        }));
				

- #### Command: Set charging mode / charging current

	`BleManager.getInstance().setChargingMode(bleDevice, current, new BleReadCallback())`

		BleManager.getInstance().setChargingMode(bleDevice, current, new BleReadCallback() {
            @Override
            public void onReadSuccess(HashMap data) {
                        
            }

            @Override
            public void onReadFailure(BleException exception) {

            }
        }));
			
	Tips：
	- current: specifies the charging current in percent [%], one digit is 1[%]. 
	- Min. value is = 1, highest value = 100

- #### Command: Read the current charging mode / charging current

	`BleManager.getInstance().readChargingMode(bleDevice, new BleReadCallback())`

		BleManager.getInstance().readChargingMode(bleDevice, new BleReadCallback() {
            @Override
            public void onReadSuccess(HashMap data) {
                 
            }

            @Override
            public void onReadFailure(BleException exception) {

            }
        }));

- #### Command: Read the size of the battery log memory	

	`BleManager.getInstance().readBatteryLogMemory(bleDevice, new BleReadCallback())`

		BleManager.getInstance().readBatteryLogMemory(bleDevice, new BleReadCallback() {
            @Override
            public void onReadSuccess(HashMap data) {
                        
            }

            @Override
            public void onReadFailure(BleException exception) {

            }
        }));

- #### Command: Read the number of battery data sets stored in the Flash/EEP memory	

	`BleManager.getInstance().readBatteryDataStored(bleDevice, new BleReadCallback())`
	
		BleManager.getInstance().readBatteryDataStored(bleDevice, new BleReadCallback() {
            @Override
            public void onReadSuccess(HashMap data) {
                        
            }

            @Override
            public void onReadFailure(BleException exception) {

            }
        }));

- #### Command: Read the battery data sets number (MSB, LSB):

	`BleManager.getInstance().readBatteryDataSetsNumber(bleDevice, msb, lsb, new BleReadCallback())`

		BleManager.getInstance().readBatteryDataSetsNumber(bleDevice, msb, lsb, new BleReadCallback() {
            @Override
            public void onReadSuccess(HashMap data) {
                        
			}

			@Override
			public void onReadFailure(BleException exception) {

			}
		}));
				
	Tips：
	- (MSB, LSB) represents the maximum number of battery log data entries / sets, which can be stored in the EEP/Flash memory (= EEP/Flash memory size).
	- The higher possible value is 65535 

- #### Command: Read the current battery data

	`BleManager.getInstance().readCurrentBatteryLogData(bleDevice, new BleReadCallback())`
	
		BleManager.getInstance().readCurrentBatteryLogData(bleDevice, new BleReadCallback() {
			@Override
			public void onReadSuccess(HashMap data) {
                        
			}

			@Override
			public void onReadFailure(BleException exception) {

			}
		}));

- #### Command: Read the charger log data

	`BleManager.getInstance().readChargerLogData(bleDevice, new BleReadCallback())`

		BleManager.getInstance().readChargerLogData(bleDevice, new BleReadCallback() {
			@Override
			public void onReadSuccess(HashMap data) {
                        
			}

			@Override
			public void onReadFailure(BleException exception) {

			}
		}));


- #### Get Rssi

	`void readRssi(BleDevice bleDevice, BleRssiCallback callback)`

        BleManager.getInstance().readRssi(
                bleDevice,
                new BleRssiCallback() {

                    @Override
                    public void onRssiFailure(BleException exception) {

                    }

                    @Override
                    public void onRssiSuccess(int rssi) {

                    }
                });

	Tips：
	- Obtaining the signal strength of the device must be carried out after the device is connected.
	- Some devices may not be able to read Rssi, do not callback onRssiSuccess (), and callback onRssiFailure () because of timeout.

- #### set Mtu

	`void setMtu(BleDevice bleDevice,
                       int mtu,
                       BleMtuChangedCallback callback)`

        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {

            }

            @Override
            public void onMtuChanged(int mtu) {

            }
        });

	Tips：
	- Setting up MTU requires operation after the device is connected.
	- There is no such restriction in the Android Version (API-17 to API-20). Therefore, only the equipment above API21 will expand the demand for MTU.
	- The parameter MTU of the method is set to 23, and the maximum setting is 512.
	- Not every device supports the expansion of MTU, which requires both sides of the communication, that is to say, the need for the device hardware also supports the expansion of the MTU method. After calling this method, you can see through onMtuChanged (int MTU) how much the maximum transmission unit of the device is expanded to after the final setup. If the device does not support, no matter how many settings, the final MTU will be 23.

- #### requestConnectionPriority

	`boolean requestConnectionPriority(BleDevice bleDevice,int connectionPriority)`

	Tips:
	- Request a specific connection priority. Must be one of{@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED}, {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH} or {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.

- #### Converte BleDevice object

	`BleDevice convertBleDevice(BluetoothDevice bluetoothDevice)`

	`BleDevice convertBleDevice(ScanResult scanResult)`

	Tips：
	- The completed BleDevice object is still unconnected, if necessary, advanced connection.

- #### Get all connected devices

	`List<BleDevice> getAllConnectedDevice()`

        BleManager.getInstance().getAllConnectedDevice();

- #### Get a BluetoothGatt of a connected device

	`BluetoothGatt getBluetoothGatt(BleDevice bleDevice)`

- #### Get all Service of a connected device

	`List<BluetoothGattService> getBluetoothGattServices(BleDevice bleDevice)`

- #### Get all the Characteristic of a Service

	`List<BluetoothGattCharacteristic> getBluetoothGattCharacteristics(BluetoothGattService service)`
		
- #### Determine whether a device has been connected

	`boolean isConnected(BleDevice bleDevice)`

        BleManager.getInstance().isConnected(bleDevice);

	`boolean isConnected(String mac)`

		BleManager.getInstance().isConnected(mac);

- #### Determine the current connection state of a device

	`int getConnectState(BleDevice bleDevice)`

		BleManager.getInstance().getConnectState(bleDevice);

- #### Disconnect a device

	`void disconnect(BleDevice bleDevice)`

        BleManager.getInstance().disconnect(bleDevice);

- #### Disconnect all devices

	`void disconnectAllDevice()`

        BleManager.getInstance().disconnectAllDevice();

- #### Out of use, clean up resources

	`void destroy()`

        BleManager.getInstance().destroy();


- #### HexUtil

    Data operation tool class

    `String formatHexString(byte[] data, boolean addSpace)`

	`byte[] hexStringToBytes(String hexString)`

	`char[] encodeHex(byte[] data, boolean toLowerCase)`


- #### BleDevice

    BLE device object is the smallest unit object of scanning, connection and operation in this framework.

    `String getName()` Bluetooth broadcast name

    `String getMac()` Bluetooth MAC

    `byte[] getScanRecord()` Broadcast data

    `int getRssi()` Initial signal intensity