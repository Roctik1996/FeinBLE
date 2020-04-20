package com.bbg.feinble.comm;

import com.bbg.feinblelib.data.BleDevice;

public interface Observer {

    void disConnected(BleDevice bleDevice);
}
