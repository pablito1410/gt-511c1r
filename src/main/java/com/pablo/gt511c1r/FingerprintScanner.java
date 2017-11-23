package com.pablo.gt511c1r;


import com.fazecast.jSerialComm.SerialPort;
import com.pablo.gt511c1r.exception.CommandProcessingException;

public class FingerprintScanner {

    private static final int BAUD_RATE = 9600;
    private final CommandProcessor commandProcessor;


    public FingerprintScanner(final String commPortName) {
        commandProcessor = new CommandProcessor(commPortName, BAUD_RATE, 1000, 0);
    }

    FingerprintScanner(final SerialPort serialPort) {
        commandProcessor = new CommandProcessor(serialPort);
    }

    public boolean isSerialPortOpened() {
        return commandProcessor.getSerialPort().isOpen();
    }

    /**
     * Initiates communication with GT-511C1R
     *
     * @return array with hardware info
     */
    public String[] open() {
        commandProcessor.openSerialPort();
        final ResponsePacket responsePacket = commandProcessor.process(Command.OPEN, 1);
        final byte[] dataBytes = responsePacket.getDataBytes();
        return HardwareInfoUtils.parseHardwareInfo(dataBytes);
    }

    /**
     * Closes communication with GT-511C1R
     */
    public void close() {
        commandProcessor.process(Command.CLOSE);
        commandProcessor.closeSerialPort();
    }

    /**
     * Turns the LED backlight on or off.
     *
     * @param on The LED backlight state
     * @return true if LED backlight state was switched successfully or false if any error has occurred
     */
    public boolean setLED(boolean on) {
        final ResponsePacket responsePacket = commandProcessor.process(Command.CMOS_LED, on ? 1 : 0);
        return responsePacket.ack();
    }

    /**
     * Checks the currently pressed finger against all enrolled fingerprints
     *
     * @return The specified ID of fingerprint (0-19)
     * @throws CommandProcessingException if failed to find the fingerprint in the database
     */
    public int identify() throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.IDENTIFY_1_N);

        if (responsePacket.isSuccess()) {
            return responsePacket.getParameter();
        } else {
            throw new CommandProcessingException(responsePacket.getError());
        }
    }

    /**
     * Checks if a finger is pressed on the GT-511C1R
     *
     * @return true if finger is pressed of false if not
     */
    public boolean isPressFinger() {
        final ResponsePacket responsePacket = commandProcessor.process(Command.IS_PRESS_FINGER);
        return responsePacket.getParameter() == 0;
    }

    /**
     * Changes the baud rate of the communication
     * @param baudRate baud rate of the communication.
     * @return true if the baud rate was changed succesully or false if not
     * @throws IllegalArgumentException if baud rate is different that:
     * 9600, 19200, 38400, 57600 or 115200
     */
//    boolean setBaudRate(int baudRate) {
//        if ((baudRate == 9600) || (baudRate == 19200) || (baudRate == 38400)
//                || (baudRate == 57600) || (baudRate == 115200)) {
//            final ResponsePacket responsePacket = process(Command.CHANGE_BAUD_RATE, baudRate);
//
//            if (responsePacket.isSuccess()) {
//                serialPort.setBaudRate(baudRate);
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        throw new IllegalArgumentException("Baud rate must be 9600, 19200, 38400, 57600 or 115200");
//    }


    /**
     * Gets the number of enrolled fingerprints
     *
     * @return The number of enrolled fingerprints or -1 if any error has occurred
     */
    public int getEnrollCount() {
        final ResponsePacket responsePacket = commandProcessor.process(Command.GET_ENROLL_COUNT);
        if (responsePacket.isSuccess()) {
            return responsePacket.getParameter();
        } else {
            return -1;
        }
    }

    /**
     * Checks whether the ID of fingerprint is in use
     *
     * @param id specified id of the fingerprint
     * @return true if ID is in use or false if not
     * @throws IllegalArgumentException   if {@code id} is not between 0-19
     * @throws CommandProcessingException if the specified id is not used
     */
    public boolean checkEnrolled(final int id) throws CommandProcessingException, IllegalArgumentException {
        checkIdRange(id);
        final ResponsePacket responsePacket = commandProcessor.process(Command.CHECK_ENROLLED, id);
        if (responsePacket.isSuccess() || responsePacket.getError() == Error.NACK_IS_NOT_USED) {
            return responsePacket.ack();
        } else {
            throw new CommandProcessingException(responsePacket.getError());
        }
    }

    /**
     * Starts the enrollments process
     *
     * @param id will be assigned to the fingerprint
     * @return true if the initialization of the enrollment process
     * was finished successfully or false if not
     * @throws CommandProcessingException if database is full
     *                                    or specified {@code id} is used
     */
    public boolean enrollStart(int id) throws CommandProcessingException {
        if (checkEnrolled(id)) {
            throw new CommandProcessingException(Error.NACK_IS_ALREADY_USED);
        }
        final ResponsePacket responsePacket = commandProcessor.process(Command.ENROLL_START, id);
        if (responsePacket.isSuccess()) {
            return responsePacket.ack();
        }
        throw new CommandProcessingException(responsePacket.getError());
    }

    /**
     * Captures the currently pressed finger into onboard RAM to use it in other commands.
     *
     * @param highQuality true for high quality image but slower processing,
     *                    or false for low quality image but faster processing.
     *                    Recommended to use high quality for enrollment and low quality
     *                    for verification/identification.
     * @return if capturing of the finger finished successfully, or false if not
     * @throws CommandProcessingException if finger is not pressed
     */
    public boolean captureFinger(boolean highQuality) throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.CAPTURE_FINGER, highQuality ? 1 : 0);
        if (responsePacket.isSuccess()) {
            return responsePacket.ack();
        }
        throw new CommandProcessingException(responsePacket.getError());
    }

    /**
     * Gets the first scan of the fingerprint
     *
     * @return true if the first enrollment was finished successfully or false if not
     * @throws CommandProcessingException if enroll failed or bad finger was pressed
     */
    public boolean enroll1() throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.ENROLL_1);
        if (responsePacket.isSuccess()) {
            return responsePacket.ack();
        }
        throw new CommandProcessingException(responsePacket.getError());
    }

    /**
     * Gets the second scan of the fingerprint
     *
     * @return true if the second enrollment was finished successfully or false if not
     * @throws CommandProcessingException if enroll failed or bad finger was pressed
     */
    public boolean enroll2() throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.ENROLL_2);
        if (responsePacket.isSuccess()) {
            return responsePacket.ack();
        }
        throw new CommandProcessingException(responsePacket.getError());
    }

    /**
     * Gets the third scan of the fingerprint
     *
     * @return true if the third enrollment was finished successfully or false if not
     * @throws CommandProcessingException if enroll failed or bad finger was pressed
     */
    public boolean enroll3() throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.ENROLL_3);
        if (responsePacket.ack()) {
            return responsePacket.ack();
        }
        throw new CommandProcessingException(responsePacket.getError());
    }

    /**
     * Deletes the specified {@code id} from the database
     *
     * @param id specified {@code id} of the fingerprint
     * @return true if the deletion was finished successfully or false if not
     * @throws IllegalArgumentException   if the specified {@code id} is not between 0-19
     * @throws CommandProcessingException if any exception occurred
     */
    public boolean deleteId(final int id) throws IllegalArgumentException, CommandProcessingException {
        checkIdRange(id);
        final ResponsePacket responsePacket = commandProcessor.process(Command.DELETE_ID, id);
        if (responsePacket.isSuccess()) {
            return responsePacket.ack();
        } else {
            throw new CommandProcessingException(responsePacket.getError());
        }

    }

    /**
     * Deletes all IDs from the database
     *
     * @return if the deletion was finished successfully or false if not
     * @throws CommandProcessingException if the database is empty
     */
    public boolean deleteAll() throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.DELETE_ALL);
        if (responsePacket.isSuccess()) {
            return responsePacket.ack();
        } else {
            throw new CommandProcessingException(responsePacket.getError());
        }
    }

//    public byte[] getImage() throws CommandProcessingException {
//        final ResponsePacket responsePacket = commandProcessor.process(Command.GET_RAW_IMAGE);
//        if (responsePacket.isSuccess() && responsePacket.ack()) {
//            return responsePacket.getDataBytes();
//        } else {
//            throw new CommandProcessingException(responsePacket.getError());
//        }
//    }

    public byte[] getTemplate(final int id) throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.GET_TEMPLATE);
        if (responsePacket.isSuccess() && responsePacket.ack()) {
            return responsePacket.getDataBytes();
        } else {
            throw new CommandProcessingException(responsePacket.getError());
        }
    }

    public void setTemplate(final byte[] template,
                            final int id,
                            final boolean duplicateCheck) throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.process(Command.SET_TEMPLATE, id);
        if (responsePacket.isSuccess() && responsePacket.ack()) {
            sendData(template);
        } else {
            throw new CommandProcessingException(responsePacket.getError());
        }
    }

    private void sendData(final byte[] data) throws CommandProcessingException {
        final ResponsePacket responsePacket = commandProcessor.sendData(data);
        if (!responsePacket.isSuccess() || !responsePacket.ack()) {
            throw new CommandProcessingException(responsePacket.getError());
        }
    }

    private void checkIdRange(final int id) {
        if (id < 0 || id > 19) {
            throw new IllegalArgumentException("id is not between 0-19");
        }
    }

}
