package com.pablo.gt511c1r;

enum Command {

    NOT_SET((byte) 0x00),        // Default value for enum. Scanner will return error if sent this.
    OPEN((byte) 0x01, 30),        // Open Initialization
    CLOSE((byte) 0x02),        // Close Termination
    USB_INTERNAL_CHECK((byte) 0x03),        // UsbInternalCheck Check if the connected USB device is valid
    CHANGE_BAUD_RATE((byte) 0x04),        // ChangeBaudrate Change UART baud rate
    SET_IAP_MODE((byte) 0x05),        // SetIAPMode Enter IAP Mode In this mode), FW Upgrade is available
    CMOS_LED((byte) 0x12),        // CmosLed Control CMOS LED
    GET_ENROLL_COUNT((byte) 0x20),        // Get enrolled fingerprint count
    CHECK_ENROLLED((byte) 0x21),        // Check whether the specified ID is already enrolled
    ENROLL_START((byte) 0x22),        // Start an enrollment
    ENROLL_1((byte) 0x23),        // Make 1st template for an enrollment
    ENROLL_2((byte) 0x24),        // Make 2nd template for an enrollment
    ENROLL_3((byte) 0x25),        // Make 3rd template for an enrollment), merge three templates into one template), save merged template to the database
    IS_PRESS_FINGER((byte) 0x26),        // Check if a finger is placed on the sensor
    DELETE_ID((byte) 0x40),        // Delete the fingerprint with the specified ID
    DELETE_ALL((byte) 0x41),        // Delete all fingerprints from the database
    VERIFY_1_1((byte) 0x50),        // Verification of the capture fingerprint image with the specified ID
    IDENTIFY_1_N((byte) 0x51),        // Identification of the capture fingerprint image with the database
    VERIFY_TEMPLATE_1_1((byte) 0x52),        // Verification of a fingerprint template with the specified ID
    IDENTIFY_TEMPLATE_1_N((byte) 0x53),        // Identification of a fingerprint template with the database
    CAPTURE_FINGER((byte) 0x60),        // Capture a fingerprint image((byte)256x256) from the sensor
    MAKE_TEMPLATE((byte) 0x61),        // Make template for transmission
    GET_IMAGE((byte) 0x62, 51846),        // Download the captured fingerprint image((byte)256x256)
    GET_RAW_IMAGE((byte) 0x63, 51846),        // Capture & Download raw fingerprint image((byte)320x240)
    GET_TEMPLATE((byte) 0x70, 512),        // Download the template of the specified ID
    SET_TEMPLATE((byte) 0x71, 0),        // Upload the template of the specified ID
    GET_DATABASE_START((byte) 0x72),        // Start database download), obsolete
    GET_DATABASE_END((byte) 0x73),        // End database download), obsolete
    UPGRADE_FIRMWARE((byte) 0x80),        // Not supported
    UPGRADE_ISOCD_IMAGE((byte) 0x81),        // Not supported
    ACK((byte) 0x30),        // Acknowledge.
    NACK((byte) 0x31),        // Non-acknowledge

    ;

    private final byte value;
    private final int dataPacketSize;

    Command(final byte value) {
        this.value = value;
        this.dataPacketSize = 0;
    }

    Command(final byte value, final int dataPacketSize) {
        this.value = value;
        this.dataPacketSize = dataPacketSize;
    }

    byte getValue() {
        return value;
    }

    int getDataPacketSize() {
        return dataPacketSize;
    }
}
