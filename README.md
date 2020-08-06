# Fein BLE

Android Bluetooth Low Energy

- Filtering, scanning, reading, writing and cancellation in a simple way.
- Support multi device connections  
- Support reconnection  
- Support configuration timeout for connect or operation 

# Usage

- #### Init
    
        instance.init(application)

- #### Open or close Bluetooth

		fun enableBluetooth()
		fun disableBluetooth()

- #### Initialization configuration

        instance
                .enableLog(true)
                .setReConnectCount(1, 5000)
	            .setSplitWriteNum(20)
	            .setConnectOverTime(10000)
                .setOperateTimeout(5000)

- #### Scan

	`scan(BleScanCallback callback)`

        instance.scan(object :BleScanCallback(){
            override fun onScanFinished(scanResultList: List<BleDevice>?) {
            }

            override fun onScanStarted(success: Boolean) {
            }

            override fun onScanning(bleDevice: BleDevice?) {
            }

	Tips:
	- The scanning and filtering process is carried out in the worker thread, so it will not affect the UI operation of the main thread. Eventually, every callback result will return to the main thread.。


- #### Connect with device


	`connect(bleDevice: BleDevice?, bleGattCallback: BleGattCallback?)`

        instance.connect(bleDevice,object : BleGattCallback(){
            override fun onStartConnect() {
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
            }

        })

	Tips:
	- On some types of phones, connectGatt must be effective on the main thread. It is very recommended that the connection process be placed in the main thread.
	- After connection failure, reconnect: the framework contains reconnection mechanism after connection failure, which can configure reconnection times and intervals. Of course, you can also call the `connect` method in `onConnectFail` callback automatically.
	- The connection is disconnected and reconnected: you can call the `connect` method again in the `onDisConnected` callback method.
	- In order to ensure the success rate of reconnection, it is recommended to reconnect after a period of interval.
	- When some models fail, they will be unable to scan devices for a short time. They can be connected directly through device objects or devices MAC without scanning.

- #### Connect with Mac

	`connect(mac: String?, bleGattCallback: BleGattCallback?)`

        instance.connect(bleDevice,object : BleGattCallback(){
            override fun onStartConnect() {
			
            }

            override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
			
            }

            override fun onConnectSuccess(bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int) {
			
            }

            override fun onDisConnected(isActiveDisConnected: Boolean, device: BleDevice?, gatt: BluetoothGatt?, status: Int) {
			
            }

        })

	Tips:
	- This method can attempt to connect directly to the BLE device around the Mac without scanning.
	- In many usage scenarios, I suggest that APP save the Mac of the user's customary device, then use this method to connect, which will greatly improve the connection efficiency.




- #### Cancel scan

	`cancelScan()`

		instance.cancelScan()

	Tips:
	- If this method is called, if it is still in the scan state, it will end immediately, and callback the `onScanFinished` method.



- #### Write

	`write(bleDevice: BleDevice?, data: ByteArray?, callback: BleWriteCallback?)`
	
		instance.write(bleDevice,data,object : BleWriteCallback(){
            override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
			
            }

            override fun onWriteFailure(exception: BleException?) {
			
            }

        })

	
- #### Read

	`read(bleDevice: BleDevice?, callback: BleReadCallback?)`
	
		instance.read(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
				
				
- #### Command: Read current communication protocol version	

	`readProtocolVersion(bleDevice: BleDevice?, callback: BleReadCallback?)`
	
		instance.readProtocolVersion(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
				

- #### Command: Set charging mode / charging current

	`setChargingMode(bleDevice: BleDevice?, current: Int, callback: BleReadCallback?)`

		instance.setChargingMode(bleDevice, current, object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
			
	Tips：
	- current: specifies the charging current in percent [%], one digit is 1[%]. 
	- Min. value is = 1, highest value = 100

- #### Command: Read the current charging mode / charging current

	`readChargingMode(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readChargingMode(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })

- #### Command: Read the size of the battery log memory	

	`readBatteryLogMemory(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readBatteryLogMemory(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })

- #### Command: Read the number of battery data sets stored in the Flash/EEP memory	

	`readBatteryDataStored(bleDevice: BleDevice?, callback: BleReadCallback?)`
	
		instance.readBatteryDataStored(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })

- #### Command: Read the battery data sets number (MSB, LSB):

	`readBatteryDataSetsNumber(bleDevice: BleDevice?, msb: Int, lsb: Int, callback: BleReadCallback?)`

		instance.readBatteryDataSetsNumber(bleDevice, msb, lsn, object : BleReadCallback() {
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }
			
		})
				
	Tips：
	- (MSB, LSB) represents the maximum number of battery log data entries / sets, which can be stored in the EEP/Flash memory (= EEP/Flash memory size).
	- The higher possible value is 65535 

- #### Command: Read the current battery data

	`readCurrentBatteryLogData(bleDevice: BleDevice?, callback: BleReadCallback?)`
	
		instance.readCurrentBatteryLogData(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })

- #### Command: Read the charger log data

	`readChargerLogData(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readChargerLogData(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
		
		
- #### Read manufacturer name

	`readManufacturerName(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readManufacturerName(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
		
- #### Read model number

	`readModelNumber(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readModelNumber(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
		
- #### Read serial number

	`readSerialNumber(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readSerialNumber(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
		
- #### Read hardware revision

	`readHardwareRevision(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readHardwareRevision(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
		
- #### Read firmware revision

	`readFirmwareRevision(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readFirmwareRevision(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
		
- #### Read software revision

	`readSoftwareRevision(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readSoftwareRevision(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })
		
- #### Read system ID

	`readSystemID(bleDevice: BleDevice?, callback: BleReadCallback?)`

		instance.readSystemID(bleDevice,object : BleReadCallback(){
            override fun onReadSuccess(data: HashMap<*, *>?) {
			
            }

            override fun onReadFailure(exception: BleException?) {
			
            }

        })


- #### Get Rssi

	`readRssi(bleDevice: BleDevice?, callback: BleReadCallback?)`

        instance.readRssi(bleDevice, object : BleRssiCallback(){
            override fun onRssiFailure(exception: BleException?) {
			
            }

            override fun onRssiSuccess(rssi: Int) {
			
            }

        })

	Tips：
	- Obtaining the signal strength of the device must be carried out after the device is connected.
	- Some devices may not be able to read Rssi, do not callback onRssiSuccess (), and callback onRssiFailure () because of timeout.

- #### set Mtu

	`setMtu(bleDevice: BleDevice?, mtu: Int, callback: BleMtuChangedCallback?)`

        instance.setMtu(bleDevice, mtu, object : BleMtuChangedCallback() {
            override fun onSetMTUFailure(exception: BleException?) {
			
            }
            override fun onMtuChanged(mtu: Int) {
			
            }
        })

	Tips：
	- Setting up MTU requires operation after the device is connected.
	- There is no such restriction in the Android Version (API-17 to API-20). Therefore, only the equipment above API21 will expand the demand for MTU.
	- The parameter MTU of the method is set to 23, and the maximum setting is 512.
	- Not every device supports the expansion of MTU, which requires both sides of the communication, that is to say, the need for the device hardware also supports the expansion of the MTU method. After calling this method, you can see through onMtuChanged (int MTU) how much the maximum transmission unit of the device is expanded to after the final setup. If the device does not support, no matter how many settings, the final MTU will be 23.

- #### requestConnectionPriority

	`requestConnectionPriority(bleDevice: BleDevice?, connectionPriority: Int): Boolean`

	Tips:
	- Request a specific connection priority. Must be one of{@link BluetoothGatt#CONNECTION_PRIORITY_BALANCED}, {@link BluetoothGatt#CONNECTION_PRIORITY_HIGH} or {@link BluetoothGatt#CONNECTION_PRIORITY_LOW_POWER}.

- #### Converte BleDevice object

	`convertBleDevice(bluetoothDevice: BluetoothDevice?): BleDevice`

	`convertBleDevice(scanResult: ScanResult?): BleDevice`

	Tips：
	- The completed BleDevice object is still unconnected, if necessary, advanced connection.

- #### Get all connected devices

	`allConnectedDevice: List<BleDevice?>?`

        instance.allConnectedDevice

- #### Get a BluetoothGatt of a connected device

	`getBluetoothGatt(bleDevice: BleDevice?): BluetoothGatt?`

- #### Get all Service of a connected device

	`getBluetoothGattServices(bleDevice: BleDevice?): List<BluetoothGattService>? `

- #### Get all the Characteristic of a Service

	`getBluetoothGattCharacteristics(service: BluetoothGattService): List<BluetoothGattCharacteristic>`
		
- #### Determine whether a device has been connected

	`isConnected(bleDevice: BleDevice?) : Boolean`

        instance.isConnected(bleDevice)

	`isConnected(mac: String) : Boolean `
 
		instance.isConnected(mac)

- #### Determine the current connection state of a device

	`getConnectState(bleDevice: BleDevice?) : Int`

		instance.getConnectState(bleDevice)

- #### Disconnect a device

	`disconnect(bleDevice: BleDevice?)`

        instance.disconnect(bleDevice)

- #### Disconnect all devices

	`disconnectAllDevice()`

        instance.disconnectAllDevice()

- #### Out of use, clean up resources

	`destroy()`

        instance.destroy()


- #### HexUtil

    Data operation tool class

    `formatHexString(data: ByteArray?, addSpace: Boolean = false): String?`

	`encodeHex(data: ByteArray?, toDigits: CharArray): CharArray?`


- #### BleDevice

    BLE device object is the smallest unit object of scanning, connection and operation in this framework.

    `name: String?` Bluetooth broadcast name

    `mac: String?` Bluetooth MAC

    `scanRecord: ByteArray?` Broadcast data

    `rssi: Int` Initial signal intensity