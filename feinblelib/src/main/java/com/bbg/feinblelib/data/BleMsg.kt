package com.bbg.feinblelib.data

object BleMsg {
    // Scan
    const val MSG_SCAN_DEVICE = 0X00

    // Connect
    const val MSG_CONNECT_FAIL = 0x01
    const val MSG_DISCONNECTED = 0x02
    const val MSG_RECONNECT = 0x03
    const val MSG_DISCOVER_SERVICES = 0x04
    const val MSG_DISCOVER_FAIL = 0x05
    const val MSG_DISCOVER_SUCCESS = 0x06
    const val MSG_CONNECT_OVER_TIME = 0x07

    // Write
    const val MSG_CHA_WRITE_START = 0x31
    const val MSG_CHA_WRITE_RESULT = 0x32
    const val KEY_WRITE_BUNDLE_STATUS = "write_status"
    const val KEY_WRITE_BUNDLE_VALUE = "write_value"

    // Read
    const val MSG_CHA_READ_START = 0x41
    const val MSG_CHA_READ_RESULT = 0x42
    const val KEY_READ_BUNDLE_STATUS = "read_status"
    const val KEY_READ_BUNDLE_VALUE = "read_value"

    // Rssi
    const val MSG_READ_RSSI_START = 0x51
    const val MSG_READ_RSSI_RESULT = 0x52
    const val KEY_READ_RSSI_BUNDLE_STATUS = "rssi_status"
    const val KEY_READ_RSSI_BUNDLE_VALUE = "rssi_value"

    // Mtu
    const val MSG_SET_MTU_START = 0x61
    const val MSG_SET_MTU_RESULT = 0x62
    const val KEY_SET_MTU_BUNDLE_STATUS = "mtu_status"
    const val KEY_SET_MTU_BUNDLE_VALUE = "mtu_value"
}