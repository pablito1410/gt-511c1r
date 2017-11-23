package com.pablo.gt511c1r;

import com.fazecast.jSerialComm.SerialPort;
import com.pablo.gt511c1r.exception.ResponsePacketParingException;

import java.util.Arrays;

class ResponsePacket {

    static final int RESPONSE_PACKET_SIZE = 12;
    private static final int DATA_PACKET_HEADER_SIZE = 4;

    static final byte COMMAND_START_CODE_1 = 0x55;    // Static byte to mark the beginning of a command packet	-	never changes
    static final int COMMAND_START_CODE_2 = 0xAA;    // Static byte to mark the beginning of a command packet	-	never changes
    static final byte COMMAND_DEVICE_ID_1 = 0x01;    // Device ID Byte 1 (lesser byte)							-	theoretically never changes
    static final byte COMMAND_DEVICE_ID_2 = 0x00;    // Device ID Byte 2 (greater byte)							-	theoretically never changes

    private final Error error;
    private final byte[] rawBytes;
    private final byte[] parameterBytes;
    private final byte[] responseBytes;
    private final boolean ack;
    private final byte[] dataBytes;


    ResponsePacket(byte[] buffer) {
        checkInputBuffer(buffer, true);
        ack = buffer[8] == Command.ACK.getValue() ? true : false;
        error = Error.getInstance(buffer[5], buffer[4]);
        parameterBytes = parseParameterBytes(buffer);
        responseBytes = parseResponseBytes(buffer);
        dataBytes = parseDataBytes(buffer);
        rawBytes = parseRawBytes(buffer);
    }

    static ResponsePacket read(final SerialPort serial, final int dataPacketSize) {
        final byte[] responseBytes = new byte[RESPONSE_PACKET_SIZE + dataPacketSize];
        serial.readBytes(responseBytes, responseBytes.length);
        return new ResponsePacket(responseBytes);
    }

    private void checkInputBuffer(final byte[] buffer, final boolean useSerialDebug) {
        checkParsing(buffer[0], COMMAND_START_CODE_1, COMMAND_START_CODE_1, "COMMAND_START_CODE_1", useSerialDebug);
        checkParsing(buffer[1], (byte) COMMAND_START_CODE_2, (byte) COMMAND_START_CODE_2, "COMMAND_START_CODE_2", useSerialDebug);
        checkParsing(buffer[2], COMMAND_DEVICE_ID_1, COMMAND_DEVICE_ID_1, "COMMAND_DEVICE_ID_1", useSerialDebug);
        checkParsing(buffer[3], COMMAND_DEVICE_ID_2, COMMAND_DEVICE_ID_2, "COMMAND_DEVICE_ID_2", useSerialDebug);

        checkParsing(buffer[8], Command.ACK.getValue(), Command.NACK.getValue(), "ackNak_LOW", useSerialDebug);
        checkParsing(buffer[9], (byte) 0x00, (byte) 0x00, "ackNak_HIGH", useSerialDebug);

        final int checksum = calculateChecksum(buffer, 10);
        final byte checksumLow = getLowByte(checksum);
        final byte checksumHigh = 1; // TODO getHighByte(checksum);
        checkParsing(buffer[10], checksumLow, checksumLow, "Checksum_LOW", useSerialDebug);
        checkParsing(buffer[11], checksumHigh, checksumHigh, "Checksum_HIGH", useSerialDebug);
    }

    private static byte[] parseParameterBytes(final byte[] buffer) {
        final byte[] paramBytes = new byte[4];
        paramBytes[0] = buffer[4];
        paramBytes[1] = buffer[5];
        paramBytes[2] = buffer[6];
        paramBytes[3] = buffer[7];
        return paramBytes;
    }

    private static byte[] parseResponseBytes(final byte[] buffer) {
        final byte[] responseBytes = new byte[2];
        responseBytes[0] = buffer[8];
        responseBytes[1] = buffer[9];
        return responseBytes;
    }

    private byte[] parseDataBytes(final byte[] buffer) {
        if (buffer.length > RESPONSE_PACKET_SIZE + DATA_PACKET_HEADER_SIZE) {
            return Arrays.copyOfRange(buffer, RESPONSE_PACKET_SIZE + DATA_PACKET_HEADER_SIZE, buffer.length - 2); // TODO sprawdzic rozmir tablicy
        } else {
            return new byte[0];
        }
    }

    private static byte[] parseRawBytes(final byte[] buffer) {
        final byte[] rawBytes = new byte[12];
        for (int i = 0; i < 12; i++) {
            rawBytes[i] = buffer[i];
        }
        return rawBytes;
    }

    // Gets an int from the parameter bytes
    private int intFromParameter() {
        int retval = 0;
        retval = (retval << 8) + parameterBytes[3];
        retval = (retval << 8) + parameterBytes[2];
        retval = (retval << 8) + parameterBytes[1];
        retval = (retval << 8) + parameterBytes[0];
        return retval;
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

    // checks to see if the byte is the proper value, and logs it to the serial channel if not
    boolean checkParsing(byte b, byte propervalue, byte alternatevalue, String varname, boolean UseSerialDebug) {
        boolean retval = (b != propervalue) && (b != alternatevalue);
        if ((UseSerialDebug) && (retval)) {
            System.out.print("Response_Packet parsing error ");
            System.out.print(varname);
            System.out.print(" ");
            System.out.print(propervalue);
            System.out.print(" || ");
            System.out.print(alternatevalue);
            System.out.print(" != ");
            System.out.println(b);
            throw new ResponsePacketParingException(varname + " -> required=" + propervalue + " found=" + b);
        }
        return retval;
    }

    boolean ack() {
        return ack;
    }

    Error getError() {
        return error;
    }

    int getParameter() {
        return intFromParameter();
    }

    byte[] getDataBytes() {
        return dataBytes;
    }

    boolean isSuccess() {
        return error == Error.NO_ERROR;
    }
}
