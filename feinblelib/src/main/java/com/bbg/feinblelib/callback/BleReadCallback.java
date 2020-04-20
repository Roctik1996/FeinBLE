package com.bbg.feinblelib.callback;

import com.bbg.feinblelib.exception.BleException;

import java.util.HashMap;

public abstract class BleReadCallback extends BleBaseCallback {

    public abstract void onReadSuccess(HashMap data);

    public abstract void onReadFailure(BleException exception);

}
