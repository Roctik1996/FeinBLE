package com.bbg.feinblelib.utils;

class Const {

    final static String protocol="PROTOCOL";
    final static String chargingMode="CHARGING_MODE";

    final static String[] currentChargingMode=new String[]{
            "CURRENT_CHARGING_MODE",
            "I_MAX_CHARGE_1",
            "I_MAX_CHARGE_2"
    };

    final static String[] batteryLogMemory=new String[]{
            "MSB",
            "LSB"
    };

    final static String[] batterySetsStored=new String[]{
            "MSB",
            "LSB"
    };

    final static String[] keyForSets = new String[]{
            "PARAMETER_MSB",
            "PARAMETER_LSB",
            "MANUFACTURING_DATE_MSB",
            "MANUFACTURING_DATE_LSB",
            "SERIAL_NUMBER_MSB",
            "SERIAL_NUMBER_LSB",
            "BATTERY_TYPE",
            "BATTERY_CAPACITY_MSB",
            "BATTERY_CAPACITY_LSB",
            "CHARGING_CYCLES_MSB",
            "CHARGING_CYCLES_LSB",
            "CHARGING_MODE",
            "CHARGING_CURRENT",
            "BATTERY_VOLTAGE_MSB",
            "BATTERY_VOLTAGE_LSB",
            "BATTERY_TEMPERATURE",
            "CHARGER_TEMPERATURE",
            "TIME_START_CHARGING",
            "TIME_CC-CV",
            "TIME_FULL_BAT_DISPLAY",
            "TIME_END_CHARGE",
            "TIME_ON_CHARGER",
            "ERROR_COUNTER",
            "EMPTY_1",
            "EMPTY_2",
            "FAN_ON_COUNTER",
            "KEY_32"
    };

    final static String[] keyForCurrentBatteryData = new String[]{
            "PARAMETER_FIRST",
            "PARAMETER_SECOND",
            "MANUFACTURING_DATE_MSB",
            "MANUFACTURING_DATE_LSB",
            "SERIAL_NUMBER_MSB",
            "SERIAL_NUMBER_LSB",
            "BATTERY_TYPE",
            "BATTERY_CAPACITY_MSB",
            "BATTERY_CAPACITY_LSB",
            "CHARGING_CYCLES_MSB",
            "CHARGING_CYCLES_LSB",
            "CHARGING_MODE",
            "ACTUAL_CHARGING_CURRENT",
            "ACTUAL_BATTERY_VOLTAGE_MSB",
            "ACTUAL_BATTERY_VOLTAGE_LSB",
            "ACTUAL_BATTERY_TEMPERATURE",
            "ACTUAL_CHARGER_TEMPERATURE",
            "ACTUAL_TIME_START_CHARGING",
            "TIME_CC-CV",
            "TIME_FULL_BAT_DISPLAY",
            "TIME_END_CHARGE",
            "TIME_ON_CHARGER",
            "ERROR_COUNTER",
            "STATUS",
            "SOC",
            "FAN_ON_COUNTER",
            "KEY_32"
    };

    final static String[] keyForCurrentChargerData = new String[]{
            "Hardware_version_MSB",
            "Hardware_version_LSB",
            "Firmware_version_MSB",
            "Firmware_version_LSB",
            "Manufacturing_year",
            "Manufacturing_month",
            "Serial_number_MSB_first",
            "Serial_number_MSB_last",
            "Serial_number_LSB_first",
            "Serial_number_LSB_last",
            "N_of_mains_MSB",
            "N_of_mains_LSB",
            "N_of_plugged_batteries_MSB",
            "N_of_plugged_batteries_LSB",
            "N_of_charged_batteries_MSB",
            "N_of_charged_batteries_LSB",
            "N_of part chg_1_MSB",
            "N_of part chg_1_LSB",
            "N_of part chg_2_MSB",
            "N_of part chg_2_LSB",
            "FAN_ON_COUNTER_MSB",
            "FAN_ON_COUNTER_LSB",
            "Fan_total_ON_time_MSB",
            "Fan_total_ON_time_LSB",
            "Total ON time_MSB_first",
            "Total ON time_MSB_last",
            "Total ON time_LSB_first",
            "Total ON time_LSB_last",
            "Time battery plugged_MSB_first",
            "Time battery plugged_MSB_last",
            "Time battery plugged_LSB_first",
            "Time battery plugged_LSB_last",
            "Waiting time_MSB_first",
            "Waiting time_MSB_last",
            "Waiting time_LSB_first",
            "Waiting time_LSB_last",
            "Time CC charge_MSB_first",
            "Time CC charge_MSB_last",
            "Time CC charge_LSB_first",
            "Time CC charge_LSB_last",
            "Time CV charge_MSB_first",
            "Time CV charge_MSB_last",
            "Time CV charge_LSB_first",
            "Time CV charge_LSB_last",
            "Time 1W_25W load_MSB_first",
            "Time 1W_25W load_MSB_last",
            "Time 1W_25W load_LSB_first",
            "Time 1W_25W load_LSB_last",
            "Time 25W_75W load_MSB_first",
            "Time 25W_75W load_MSB_last",
            "Time 25W_75W load_LSB_first",
            "Time 25W_75W load_LSB_last",
            "Time 75W_140W load_MSB_first",
            "Time 75W_140W load_MSB_last",
            "Time 75W_140W load_LSB_first",
            "Time 75W_140W load_LSB_last",
            "Time max. load_MSB_first",
            "Time max. load_MSB_last",
            "Time max. load_LSB_first",
            "Time max. load_LSB_last",
    };


}
