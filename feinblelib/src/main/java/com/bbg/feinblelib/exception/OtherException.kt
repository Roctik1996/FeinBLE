package com.bbg.feinblelib.exception

class OtherException(description: String) : BleException(BleException.Companion.ERROR_CODE_OTHER, description)