package com.bbg.feinblelib.utils;

public class Utils {

    /**
     * add checksum to command
     *
     * @param data - array of bytes(without checksum)
     * @return array with checksum
     */
    private static byte[] addChecksumToCommand(int n, byte[] data, byte cs)
    {
        byte[] cs_data = new byte[n + 1];
        if (n >= 0) System.arraycopy(data, 0, cs_data, 0, n);
        cs_data[n] = cs;

        return cs_data;
    }

    /**
     * calculated checksum
     *
     * @param data - array of bytes(without checksum)
     * @return array with checksum
     */
    public static byte[] dataWithChecksum(byte[] data){
        byte sum = 0;
        for (byte b : data) {
            sum = (byte) (sum + b);
        }
        byte cs= (byte) (~sum+1);
        return addChecksumToCommand(data.length,data,cs);
    }
}
