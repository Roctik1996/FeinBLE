package com.bbg.feinblelib.utils;

import java.util.HashMap;

import static com.bbg.feinblelib.utils.Const.batteryLogMemory;
import static com.bbg.feinblelib.utils.Const.batterySetsStored;
import static com.bbg.feinblelib.utils.Const.chargingMode;
import static com.bbg.feinblelib.utils.Const.currentChargingMode;
import static com.bbg.feinblelib.utils.Const.keyForCurrentBatteryData;
import static com.bbg.feinblelib.utils.Const.keyForCurrentChargerData;
import static com.bbg.feinblelib.utils.Const.keyForSets;
import static com.bbg.feinblelib.utils.Const.protocol;

public class Parser {


    /**
     * Response parser
     *
     * @param data - array of bytes
     *
     * @return parsed value
     */
    public static HashMap parseCommand(byte[] data) {
        HashMap<String, String> parseResult = new HashMap<>();
        StringBuilder result=new StringBuilder();
        if (data[data.length-1]==getChecksum(data)) {
            switch (getCommand(data)) {
                case "0x0010": // read communication protocol version
                    for (int i = 5; i < data.length - 1; i++) {
                        result.append(data[i] & 0xFF).append(".");
                    }
                    parseResult.put(protocol, result.toString());
                    return parseResult;

                case "0x0040": // set charging mode / charging current
                    for (int i = 6; i < data.length - 1; i++) {
                        result.append(data[i] & 0xFF);
                    }
                    parseResult.put(chargingMode, result.toString());
                    return parseResult;

                case "0x0041": // read charging mode
                    for (int i = 6; i < data.length - 1; i++) {
                        parseResult.put(currentChargingMode[i-6], String.valueOf(data[i] & 0xFF));
                    }
                    return parseResult;

                case "0x0080": // read the size of the battery log memory
                    return getValues(data,batteryLogMemory);

                case "0x0082": // read number of battery data sets stored
                    return getValues(data,batterySetsStored);

                case "0x0084": // read battery data set number
                    return getValues(data, keyForSets);


                case "0x0085": //read current battery data sets stored
                    return getValues(data, keyForCurrentBatteryData);

                case "0x0090": // read charger status
                    return getValues(data, keyForCurrentChargerData);
            }
        }
        return parseResult;
    }

    private static HashMap getValues(byte[] data, String... keys) {
        HashMap<String, String> parseResult = new HashMap<>();
        int k = 0;
            for (int i = 5; i < data.length - 1; i++) {
                parseResult.put(keys[k], String.valueOf(data[i] & 0xFF));
                k++;
            }
            return parseResult;
    }

    private static byte getChecksum(byte[] data){
        byte sum = 0;
        for (int i=0;i<data.length-1;i++) {
            sum = (byte) (sum + data[i] & 0xFF);
        }
        return (byte) (~sum+1);
    }

    private static String getCommand(byte[] data) {
        String cmd = "";
        if (data.length >= 8) {
            cmd = "0x0" + Integer.toHexString(data[3]) + Integer.toHexString(data[4] & 0xFF);
        }
        return cmd;
    }
}
