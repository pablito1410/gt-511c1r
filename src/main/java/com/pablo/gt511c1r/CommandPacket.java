package com.pablo.gt511c1r;

import com.fazecast.jSerialComm.SerialPort;

class CommandPacket {

    private static final byte COMMAND_START_CODE_1 = 0x55;
    private static final int COMMAND_START_CODE_2 = 0xAA;
    private static final byte COMMAND_DEVICE_ID_1 = 0x01;
    private static final byte COMMAND_DEVICE_ID_2 = 0x00;

    private final Command command;
    private final byte[] parameter;
    private final byte[] commandBytes;

    /**
     * @param command
     * @param parameter
     */
    CommandPacket(final Command command, final int parameter) {
        this(command, parameterToBytes(parameter));
    }

    /**
     * @param command
     * @param parameter
     */
    CommandPacket(final Command command, final byte[] parameter) {
        this.command = command;
        this.parameter = parameter;
        this.commandBytes = commandToBytes(command);
    }

    /**
     * Converts the int to bytes and puts them into the paramter array
     */
    private static byte[] parameterToBytes(int i) {
        final byte[] param = new byte[4];
        param[0] = (byte) (i & 0x000000ff);
        param[1] = (byte) ((i & 0x0000ff00) >> 8);
        param[2] = (byte) ((i & 0x00ff0000) >> 16);
        param[3] = (byte) ((i & 0xff000000) >> 24);
        return param;
    }

    private static byte[] commandToBytes(final Command command) {
        final int cmd = command.getValue();
        final byte[] bytes = new byte[2];
        bytes[0] = getLowByte(cmd);
        bytes[1] = getHighByte(cmd);
        return bytes;
    }

    private byte[] getPacketBytes() {
        final int checksum = calculateChecksum();
        final byte[] packetBytes = createPacketBytes(checksum);
        return packetBytes;
    }

    private byte[] createPacketBytes(final int checksum) {
        final byte[] packetBytes = new byte[12];
        packetBytes[0] = COMMAND_START_CODE_1;
        packetBytes[1] = (byte) COMMAND_START_CODE_2;
        packetBytes[2] = COMMAND_DEVICE_ID_1;
        packetBytes[3] = COMMAND_DEVICE_ID_2;
        packetBytes[4] = parameter[0];
        packetBytes[5] = parameter[1];
        packetBytes[6] = parameter[2];
        packetBytes[7] = parameter[3];
        packetBytes[8] = commandBytes[0];
        packetBytes[9] = commandBytes[1];
        packetBytes[10] = getLowByte(checksum);
        packetBytes[11] = getHighByte(checksum);
        return packetBytes;
    }

    // Returns the high byte from a word
    private static byte getHighByte(int w) {
        return (byte) ((w >> 8) & 0x00FF);
    }

    // Returns the low byte from a word
    private static byte getLowByte(int w) {
        return (byte) (w & 0x00FF);
    }

    private int calculateChecksum() {
        int w = 0;
        w += COMMAND_START_CODE_1;
        w += COMMAND_START_CODE_2;
        w += COMMAND_DEVICE_ID_1;
        w += COMMAND_DEVICE_ID_2;
        w += parameter[0];
        w += parameter[1];
        w += parameter[2];
        w += parameter[3];
        w += commandBytes[0];
        w += commandBytes[1];
        return w;
    }

    int send(final SerialPort serial) {
        if (!serial.isOpen()) {
            System.out.println("Serial port is not opened.");
            throw new IllegalArgumentException("Serial port is not opened");
        }
        final byte[] packetBytes = getPacketBytes();
        return serial.writeBytes(packetBytes, packetBytes.length);
    }
}
