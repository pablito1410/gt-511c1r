package com.pablo.gt511c1r;

import java.util.Objects;

final class HardwareInfoUtils {

    private HardwareInfoUtils() {
    }


    /**
     * Parses a data packet bytes of response to the array of hardware info
     *
     * @param bytes bytes of data packet
     * @return array of hardware info
     * @throws NullPointerException if {@code bytes} is null
     */
    static String[] parseHardwareInfo(final byte[] bytes) {
        Objects.requireNonNull(bytes);
        if (bytes.length < 24) {
            throw new IllegalArgumentException("length of bytes cannot be shorter than 26");
        }
        final String[] hardwareInfo = new String[3];
        hardwareInfo[0] = "Firmware version: " + firmwareVersion(bytes);
        hardwareInfo[1] = "ISO Area max size: " + isoAreaMaxSize(bytes);
        hardwareInfo[2] = "Serial number: " + serialNumber(bytes);
        return hardwareInfo;
    }

    private static String firmwareVersion(byte[] dataBytes) {
        return String.format("%02x", dataBytes[3])
                + String.format("%02x", dataBytes[2])
                + String.format("%02x", dataBytes[1])
                + String.format("%02x", dataBytes[0]);
    }

    private static int isoAreaMaxSize(final byte[] dataBytes) {
        return dataBytes[4] + dataBytes[5] + dataBytes[6] + dataBytes[7];
    }

    private static String serialNumber(final byte[] dataBytes) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 8; i < dataBytes.length; i++) {
            sb.append(String.format("%02x", dataBytes[i]));
        }
        return sb.toString();
    }

}
