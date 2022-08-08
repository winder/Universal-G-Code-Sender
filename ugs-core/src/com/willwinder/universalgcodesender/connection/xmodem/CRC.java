package com.willwinder.universalgcodesender.connection.xmodem;

/**
 * Created by asirotinkin on 11.11.2014.
 * <p>
 * Original work from here: https://github.com/aesirot/ymodem
 */
public interface CRC {
    int getCRCLength();

    long calcCRC(byte[] block);
}
