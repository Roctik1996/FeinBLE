package com.bbg.feinblelib.exception

import java.io.Serializable

abstract class BleException(private var code: Int, private var description: String) : Serializable {

    fun setCode(code: Int): BleException {
        this.code = code
        return this
    }

    fun setDescription(description: String): BleException {
        this.description = description
        return this
    }

    override fun toString(): String {
        return "BleException { " +
                "code=" + code +
                ", description='" + description + '\'' +
                '}'
    }

    companion object {
        private const val serialVersionUID = 8004414918500865564L
        const val ERROR_CODE_TIMEOUT = 100
        const val ERROR_CODE_GATT = 101
        const val ERROR_CODE_OTHER = 102
    }

}