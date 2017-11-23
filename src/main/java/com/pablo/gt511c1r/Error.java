package com.pablo.gt511c1r;

import java.util.Objects;

public enum Error {

    NO_ERROR					((byte)0x0000, "No error"),
    NACK_TIMEOUT				((byte)0x1001, "Timeout"),
    NACK_INVALID_BAUDRATE		((byte)0x1002, "Invalid baud rate"),
    NACK_INVALID_POS			((byte)0x1003, "The specified ID is not between 0-19"),
    NACK_IS_NOT_USED			((byte)0x1004, "The specified ID is not used"),
    NACK_IS_ALREADY_USED		((byte)0x1005, "The specified ID is already used"),
    NACK_COMM_ERR				((byte)0x1006, "Communication error"),
    NACK_VERIFY_FAILED			((byte)0x1007, "1:1 verification vailure"),
    NACK_IDENTIFY_FAILED		((byte)0x1008, "1:N identification failure"),
    NACK_DB_IS_FULL				((byte)0x1009, "The database is full"),
    NACK_DB_IS_EMPTY			((byte)0x100A, "The database is empty"),
    NACK_TURN_ERR				((byte)0x100B, "Invalid order of the enrollment (The order was not as: EnrollStart -> Enroll1 -> Enroll2 -> Enroll3)"),
    NACK_BAD_FINGER				((byte)0x100C, "Bad fingerprint"),
    NACK_ENROLL_FAILED			((byte)0x100D, "Enrollment failure"),
    NACK_IS_NOT_SUPPORTED		((byte)0x100E, "The specified command is not supported"),
    NACK_DEV_ERR				((byte)0x100F, "Device error. Especially if Crypto-Chip is trouble"),
    NACK_CAPTURE_CANCELED		((byte)0x1010, "The capturing is canceled"),
    NACK_INVALID_PARAM			((byte)0x1011, "Invalid parameter"),
    NACK_FINGER_IS_NOT_PRESSED	((byte)0x1012, "Finger is not pressed"),
    INVALID						((byte)0XFFFF, "Invalid"),

    ;

    private final byte value;
    private final String message;

    /**
     * Constructs
     *
     * @param value
     * @param message
     * @throws NullPointerException if {@code message} is null
     */
    Error(final byte value, final String message) {
        this.value = value;
        this.message = Objects.requireNonNull(message);
    }

    byte getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }

    static Error getInstance(byte high, byte low) {
        Error e = INVALID;
        if (high == 0x00) {
            e = NO_ERROR;
        } else {
            switch (low) {
                case 0x00: e = NO_ERROR; break;
                case 0x01: e = NACK_TIMEOUT; break;
                case 0x02: e = NACK_INVALID_BAUDRATE; break;
                case 0x03: e = NACK_INVALID_POS; break;
                case 0x04: e = NACK_IS_NOT_USED; break;
                case 0x05: e = NACK_IS_ALREADY_USED; break;
                case 0x06: e = NACK_COMM_ERR; break;
                case 0x07: e = NACK_VERIFY_FAILED; break;
                case 0x08: e = NACK_IDENTIFY_FAILED; break;
                case 0x09: e = NACK_DB_IS_FULL; break;
                case 0x0A: e = NACK_DB_IS_EMPTY; break;
                case 0x0B: e = NACK_TURN_ERR; break;
                case 0x0C: e = NACK_BAD_FINGER; break;
                case 0x0D: e = NACK_ENROLL_FAILED; break;
                case 0x0E: e = NACK_IS_NOT_SUPPORTED; break;
                case 0x0F: e = NACK_DEV_ERR; break;
                case 0x10: e = NACK_CAPTURE_CANCELED; break;
                case 0x11: e = NACK_INVALID_PARAM; break;
                case 0x12: e = NACK_FINGER_IS_NOT_PRESSED; break;
            }
        }
        return e;
    }

}
