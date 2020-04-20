package com.bbg.feinblelib.callback;

import com.bbg.feinblelib.data.BleDevice;

public interface BleScanPresenterImp {

    void onScanStarted(boolean success);

    void onScanning(BleDevice bleDevice);

}
