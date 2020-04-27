package com.bbg.feinblelib.exception

class TimeoutException : BleException(BleException.Companion.ERROR_CODE_TIMEOUT, "Timeout Exception Occurred!")