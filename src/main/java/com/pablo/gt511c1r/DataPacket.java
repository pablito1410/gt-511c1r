package com.pablo.gt511c1r;

import com.fazecast.jSerialComm.SerialPort;

class DataPacket {

    static final byte COMMAND_START_CODE_1 = 0x5A;    // Static byte to mark the beginning of a command packet	-	never changes
    private static final int COMMAND_START_CODE_2 = 0xA5;    // Static byte to mark the beginning of a command packet	-	never changes
    private static final byte COMMAND_DEVICE_ID_1 = 0x01;    // Device ID Byte 1 (lesser byte)							-	theoretically never changes
    private static final byte COMMAND_DEVICE_ID_2 = 0x00;    // Device ID Byte 2 (greater byte)							-	theoretically never changes

    private final byte[] rawBytes;
    private final byte[] dataBytes;
//    private final boolean ack;

    private DataPacket(final byte[] dataBytes, final byte[] rawBytes) {
        this.dataBytes = dataBytes;
        this.rawBytes = rawBytes;
    }

    static DataPacket responseBody(byte[] data) {
        checkInputBuffer(data, true);
//        final boolean ack = data[8] == 0x30 ? true : false;
        final byte[] dataBytes = parseDataBytes(data);
        final byte[] rawBytes = parseRawBytes(data);
        return new DataPacket(dataBytes, rawBytes);
    }

    static DataPacket commandBody(byte[] data) {
        final byte[] packetBytes = createPacketBytes(data);
        return new DataPacket(data, packetBytes);
    }

    private static byte[] createPacketBytes(final byte[] data) {
        final byte[] packetBytes = new byte[data.length + 6];

        packetBytes[0] = COMMAND_START_CODE_1;
        packetBytes[1] = (byte) COMMAND_START_CODE_2;
        packetBytes[2] = COMMAND_DEVICE_ID_1;
        packetBytes[3] = COMMAND_DEVICE_ID_2;

        for (int i = 4; i < packetBytes.length - 2; i++) {
            packetBytes[i] = data[i - 4];
        }

        final int checksum = calculateChecksum(packetBytes, packetBytes.length - 2);
        packetBytes[packetBytes.length - 2] = getLowByte(checksum);
        packetBytes[packetBytes.length - 1] = getHighByte(checksum);

//        final byte[] output = new byte[packetBytes.length];
//        for (int i = 0; i < output.length; i++) {
//            output[i] = (byte)packetBytes[i];
//        }
        return packetBytes;
    }

    private static void checkInputBuffer(final byte[] buffer, final boolean useSerialDebug) {
        checkParsing(buffer[0], COMMAND_START_CODE_1, COMMAND_START_CODE_1, "COMMAND_START_CODE_1", useSerialDebug);
        checkParsing(buffer[1], (byte) COMMAND_START_CODE_2, (byte) COMMAND_START_CODE_2, "COMMAND_START_CODE_2", useSerialDebug);
        checkParsing(buffer[2], COMMAND_DEVICE_ID_1, COMMAND_DEVICE_ID_1, "COMMAND_DEVICE_ID_1", useSerialDebug);
        checkParsing(buffer[3], COMMAND_DEVICE_ID_2, COMMAND_DEVICE_ID_2, "COMMAND_DEVICE_ID_2", useSerialDebug);

        final int checksum = calculateChecksum(buffer, buffer.length - 2);
        final byte checksumLow = getLowByte(checksum);
        final byte checksumHigh = (byte) (getHighByte(checksum) + 0x01);
        checkParsing(buffer[28], checksumLow, checksumLow, "Checksum_LOW", useSerialDebug);
        checkParsing(buffer[29], checksumHigh, checksumHigh, "Checksum_HIGH", useSerialDebug);
    }

    private static byte[] parseDataBytes(final byte[] buffer) {
        final byte[] dataBytes = new byte[buffer.length - 6];
        for (int i = 4; i < buffer.length - 2; i++) {
            dataBytes[i - 4] = buffer[i];
        }
        return dataBytes;
    }

    private static byte[] parseRawBytes(final byte[] buffer) {
        final byte[] rawBytes = new byte[12];
        for (int i = 0; i < 12; i++) {
            rawBytes[i] = buffer[i];
        }
        return rawBytes;
    }

    // calculates the checksum from the bytes in the packet
    private static int calculateChecksum(byte[] buffer, int length) {
        int checksum = 0;
        for (int i = 0; i < length; i++) {
            checksum += buffer[i] & 0xFF;
        }
        return checksum;
    }

    // Returns the high byte from a word
    private static byte getHighByte(int w) {
        return (byte) ((w >> 8) & 0x00FF);
    }

    // Returns the low byte from a word
    private static byte getLowByte(int w) {
        return (byte) (w & 0x00FF);
    }

    private static boolean checkParsing(byte b, byte propervalue, byte alternatevalue, String varname, boolean UseSerialDebug) {
        boolean retval = (b != propervalue) && (b != alternatevalue);
        if ((UseSerialDebug) && (retval)) {
            System.out.print("Data_Packet parsing error ");
            System.out.print(varname);
            System.out.print(" ");
            System.out.print(propervalue);
            System.out.print(" || ");
            System.out.print(alternatevalue);
            System.out.print(" != ");
            System.out.println(b);
//            throw new PacketParsingException() TODO rzucić wyjątek
        }
        return retval;
    }

    int send(final SerialPort serial) {
        if (!serial.isOpen()) {
            System.out.println("Serial port is not opened.");
            throw new IllegalArgumentException("Serial port is not opened");
        }
        return serial.writeBytes(rawBytes, rawBytes.length);
    }
}
