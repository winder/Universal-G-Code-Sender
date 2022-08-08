package com.willwinder.universalgcodesender.connection.xmodem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * This is core Modem class supporting XModem (and some extensions XModem-1K, XModem-CRC), and YModem.<br/>
 * YModem support is limited (currently block 0 is ignored).<br/>
 * <br/>
 * Created by Anton Sirotinkin (aesirot@mail.ru), Moscow 2014 <br/>
 * I hope you will find this program useful.<br/>
 * You are free to use/modify the code for any purpose, but please leave a reference to me.
 * <p>
 * Original work from here: https://github.com/aesirot/ymodem
 */
class XModem {

    /* Protocol characters used */
    protected static final byte SOH = 0x01; /* Start Of Header */
    protected static final byte STX = 0x02; /* Start Of Text (used like SOH but means 1024 block size) */
    protected static final byte EOT = 0x04; /* End Of Transmission */
    protected static final byte ACK = 0x06; /* ACKnowlege */
    protected static final byte NAK = 0x15; /* Negative AcKnowlege */
    protected static final byte CAN = 0x18; /* CANcel character */

    protected static final byte CPM_EOF = 0x1A;
    protected static final byte ST_C = 'C';

    protected static final int MAX_ERRORS = 10;

    protected static final int BLOCK_TIMEOUT = 3000;
    protected static final int REQUEST_TIMEOUT = 3000;
    protected static final int WAIT_FOR_RECEIVER_TIMEOUT = 60_000;
    protected static final int SEND_BLOCK_TIMEOUT = 10_000;

    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final byte[] shortBlockBuffer;
    private final byte[] longBlockBuffer;

    /**
     * Constructor
     *
     * @param inputStream  stream for reading received data from other side
     * @param outputStream stream for writing data to other side
     */
    public XModem(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        shortBlockBuffer = new byte[128];
        longBlockBuffer = new byte[1024];
    }


    /**
     * Wait for receiver request for transmission
     *
     * @param timer
     * @return TRUE if receiver requested CRC-16 checksum, FALSE if 8bit checksum
     * @throws java.io.IOException
     */
    protected boolean waitReceiverRequest(Timer timer) throws IOException {
        int character;
        while (true) {
            try {
                character = readByte(timer);
                if (character == NAK)
                    return false;
                if (character == ST_C) {
                    return true;
                }
            } catch (TimeoutException e) {
                throw new IOException("Timeout waiting for receiver");
            }
        }
    }

    /**
     * Send data as an input stream
     * <p>
     * This method support correct thread interruption, when thread is interrupted "cancel of transmission" will be sent.
     * So you can move long transmission to other thread and interrupt it according to your algorithm.
     *
     * @param inputStream a byte array to send
     * @param useBlock1K  uses a 1K send block (XModem-1K)
     * @throws java.io.IOException if the transmission failed
     */
    public void send(InputStream inputStream, boolean useBlock1K) throws IOException {
        Timer timer = new Timer(WAIT_FOR_RECEIVER_TIMEOUT).start();

        boolean useCRC16 = waitReceiverRequest(timer);
        CRC crc;
        if (useCRC16)
            crc = new CRC16();
        else
            crc = new CRC8();

        byte[] block;
        if (useBlock1K)
            block = new byte[1024];
        else
            block = new byte[128];
        sendDataBlocks(inputStream, 1, crc, block);

        sendEOT();
    }

    protected void sendDataBlocks(InputStream dataStream, int blockNumber, CRC crc, byte[] block) throws IOException {
        int dataLength;
        while ((dataLength = dataStream.read(block)) != -1) {
            sendBlock(blockNumber++, block, dataLength, crc);
        }
    }

    protected void sendEOT() throws IOException {
        int errorCount = 0;
        Timer timer = new Timer(BLOCK_TIMEOUT);
        int character;
        while (errorCount < 10) {
            sendByte(EOT);
            try {
                character = readByte(timer.start());

                if (character == ACK) {
                    return;
                } else if (character == CAN) {
                    throw new IOException("Transmission terminated");
                }
            } catch (TimeoutException ignored) {
            }
            errorCount++;
        }
    }

    protected void sendBlock(int blockNumber, byte[] block, int dataLength, CRC crc) throws IOException {
        int errorCount;
        int character;
        Timer timer = new Timer(SEND_BLOCK_TIMEOUT);

        if (dataLength < block.length) {
            // Fill the remaining bytes with EOF:s
            for (int i = dataLength; i < block.length; i++) {
                block[i] = CPM_EOF;
            }
        }
        errorCount = 0;

        while (errorCount < MAX_ERRORS) {
            timer.start();

            if (block.length == 1024) {
                outputStream.write(STX);
            } else { //128
                outputStream.write(SOH);
            }
            outputStream.write(blockNumber);
            outputStream.write(~blockNumber);

            outputStream.write(block);
            writeCRC(block, crc);
            outputStream.flush();

            while (true) {
                try {
                    character = readByte(timer);
                    if (character == ACK) {
                        return;
                    } else if (character == NAK) {
                        errorCount++;
                        break;
                    } else if (character == CAN) {
                        throw new IOException("Transmission terminated");
                    }
                } catch (TimeoutException e) {
                    errorCount++;
                    break;
                }
            }

        }

        throw new IOException("Too many errors caught, abandoning transfer");
    }

    private void writeCRC(byte[] block, CRC crc) throws IOException {
        byte[] crcBytes = new byte[crc.getCRCLength()];
        long crcValue = crc.calcCRC(block);
        for (int i = 0; i < crc.getCRCLength(); i++) {
            crcBytes[crc.getCRCLength() - i - 1] = (byte) ((crcValue >> (8 * i)) & 0xFF);
        }
        outputStream.write(crcBytes);
    }

    /**
     * Receives data
     * <p>
     * This method support correct thread interruption, when thread is interrupted "cancel of transmission" will be sent.
     * So you can move long transmission to other thread and interrupt it according to your algorithm.
     *
     * @param outputStream the stream to receive the data into
     * @throws java.io.IOException if the transmission failed
     */
    public void receive(OutputStream outputStream, boolean useCRC16) throws IOException {
        int available;
        // clean input stream
        if ((available = inputStream.available()) > 0) {
            inputStream.skip(available);
        }

        int character = requestTransmissionStart(useCRC16);

        CRC crc;
        if (useCRC16)
            crc = new CRC16();
        else
            crc = new CRC8();

        processDataBlocks(crc, character, outputStream);
    }

    protected void processDataBlocks(CRC crc, int blockInitialCharacter, OutputStream dataOutput) throws IOException {
        // read blocks until EOT
        boolean result = false;
        boolean shortBlock;
        int blockNumber = 1;
        byte[] block;
        while (true) {
            int errorCount = 0;
            if (blockInitialCharacter == EOT) {
                // end of transmission
                sendByte(ACK);
                return;
            }

            //read and process block
            shortBlock = (blockInitialCharacter == SOH);
            try {
                block = readBlock(blockNumber, shortBlock, crc);
                dataOutput.write(block);
                blockNumber++;
                errorCount = 0;
                result = true;
                sendByte(ACK);
            } catch (TimeoutException | InvalidBlockException e) {
                errorCount++;
                if (errorCount == MAX_ERRORS) {
                    interruptTransmission();
                    throw new IOException("Transmission aborted, error count exceeded max");
                }
                sendByte(NAK);
                result = false;
            } catch (RepeatedBlockException e) {
                //thats ok, accept and wait for next block
                sendByte(ACK);
            } catch (SynchronizationLostException e) {
                //fatal transmission error
                interruptTransmission();
                throw new IOException("Fatal transmission error", e);
            }

            //wait for next block
            blockInitialCharacter = readNextBlockStart(result);
        }
    }

    protected void sendByte(byte b) throws IOException {
        outputStream.write(b);
        outputStream.flush();
    }

    /**
     * Request transmission start and return first byte of "first" block from sender (block 1 for XModem, block 0 for YModem)
     *
     * @param useCRC16
     * @return
     * @throws java.io.IOException
     */
    protected int requestTransmissionStart(boolean useCRC16) throws IOException {
        int character;
        int errorCount = 0;
        byte requestStartByte;
        if (!useCRC16) {
            requestStartByte = NAK;
        } else {
            requestStartByte = ST_C;
        }

        // wait for first block start
        Timer timer = new Timer(REQUEST_TIMEOUT);
        while (errorCount < MAX_ERRORS) {
            // request transmission start (will be repeated after 10 second timeout for 10 times)
            sendByte(requestStartByte);
            timer.start();
            try {
                while (true) {
                    character = readByte(timer);

                    if (character == SOH || character == STX) {
                        return character;
                    }
                }
            } catch (TimeoutException ignored) {
                errorCount++;
            }
        }
        interruptTransmission();
        throw new RuntimeException("Timeout, no data received from transmitter");
    }

    protected int readNextBlockStart(boolean lastBlockResult) throws IOException {
        int character;
        int errorCount = 0;
        Timer timer = new Timer(BLOCK_TIMEOUT);
        while (true) {
            timer.start();
            try {
                while (true) {
                    character = readByte(timer);

                    if (character == SOH || character == STX || character == EOT) {
                        return character;
                    }
                }
            } catch (TimeoutException ignored) {
                // repeat last block result and wait for next block one more time
                if (++errorCount < MAX_ERRORS) {
                    sendByte(lastBlockResult ? ACK : NAK);
                } else {
                    interruptTransmission();
                    throw new RuntimeException("Timeout, no data received from transmitter");
                }
            }
        }
    }

    private void shortSleep() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            try {
                interruptTransmission();
            } catch (IOException ignore) {
            }
            throw new RuntimeException("Transmission was interrupted", e);
        }
    }

    /**
     * send CAN to interrupt seance
     *
     * @throws java.io.IOException
     */
    protected void interruptTransmission() throws IOException {
        sendByte(CAN);
        sendByte(CAN);
    }

    protected byte[] readBlock(int blockNumber, boolean shortBlock, CRC crc) throws IOException, TimeoutException, RepeatedBlockException, SynchronizationLostException, InvalidBlockException {
        byte[] block;
        Timer timer = new Timer(BLOCK_TIMEOUT).start();

        if (shortBlock) {
            block = shortBlockBuffer;
        } else {
            block = longBlockBuffer;
        }
        byte character;

        character = readByte(timer);

        if (character == blockNumber - 1) {
            // this is repeating of last block, possible ACK lost
            throw new RepeatedBlockException();
        }
        if (character != blockNumber) {
            // wrong block - fatal loss of synchronization
            throw new SynchronizationLostException();
        }

        character = readByte(timer);

        if (character != ~blockNumber) {
            throw new InvalidBlockException();
        }

        // data
        for (int i = 0; i < block.length; i++) {
            block[i] = readByte(timer);
        }

        while (true) {
            if (inputStream.available() >= crc.getCRCLength()) {
                if (crc.calcCRC(block) != readCRC(crc)) {
                    throw new InvalidBlockException();
                }
                break;
            }

            shortSleep();

            if (timer.isExpired()) {
                throw new TimeoutException();
            }
        }

        return block;
    }

    private long readCRC(CRC crc) throws IOException {
        long checkSum = 0;
        for (int j = 0; j < crc.getCRCLength(); j++) {
            checkSum = (checkSum << 8) + inputStream.read();
        }
        return checkSum;
    }

    private byte readByte(Timer timer) throws IOException, TimeoutException {
        while (true) {
            if (inputStream.available() > 0) {
                int b = inputStream.read();
                return (byte) b;
            }
            if (timer.isExpired()) {
                throw new TimeoutException();
            }
            shortSleep();
        }
    }

    class RepeatedBlockException extends Exception {
    }

    class SynchronizationLostException extends Exception {
    }

    class InvalidBlockException extends Exception {
    }

}
