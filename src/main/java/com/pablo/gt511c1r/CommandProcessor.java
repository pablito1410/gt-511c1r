package com.pablo.gt511c1r;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Objects;

class CommandProcessor {

    private final SerialPort serialPort;

    CommandProcessor(final SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public CommandProcessor(final String commPortName,
                            final int baudRate,
                            final int readTimeout,
                            final int writeTimeout) {
        Objects.requireNonNull(commPortName);
        serialPort = SerialPort.getCommPort(commPortName);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, readTimeout, writeTimeout);
        serialPort.setBaudRate(baudRate);
    }

    SerialPort getSerialPort() {
        return serialPort; // TODO wyciagnac serialport wyzej
    }

    void openSerialPort() {
        serialPort.openPort();
    }

    void closeSerialPort() {
        serialPort.closePort();
    }

    /**
     * Processing command
     *
     * @param command   command to process
     * @param parameter parameter of the command
     * @return response packet of the command
     */
    ResponsePacket process(final Command command, final int parameter) {
        sendCommand(serialPort, command, parameter);
        return readResponse(serialPort, command.getDataPacketSize());
    }

    /**
     * Processing command with the parameter 0
     *
     * @param command command to process
     * @return response packet of the command
     */
    ResponsePacket process(final Command command) {
        sendCommand(serialPort, command, 0);
        return readResponse(serialPort, command.getDataPacketSize());
    }

    /**
     * Sends command to GT-511C1R through the serialPort port
     *
     * @param serial    instance of the serialPort port
     * @param command   command of the GT-511C1R
     * @param parameter parameter of the command
     * @return number of the bytes sent
     */
    private static int sendCommand(final SerialPort serial, final Command command, final int parameter) {
        final CommandPacket commandPacket = new CommandPacket(command, parameter);
        return commandPacket.send(serial);
    }

    /**
     * Reads response from GT-511C1R through the serialPort port
     *
     * @param serial         instance of the serialPort port
     * @param dataPacketSize size of the data packet in a response packet
     * @return response packet from GT-511C1R
     */
    private static ResponsePacket readResponse(final SerialPort serial, final int dataPacketSize) {
        final ResponsePacket responsePacket = ResponsePacket.read(serial, dataPacketSize);
        return responsePacket;
    }

    ResponsePacket sendData(final byte[] data) {
        sendData(serialPort, data);
        return readResponse(serialPort, 0);
    }

    private static int sendData(final SerialPort serial, final byte[] data) {
        final DataPacket dataPacket = DataPacket.commandBody(data);
        return dataPacket.send(serial);
    }

}
