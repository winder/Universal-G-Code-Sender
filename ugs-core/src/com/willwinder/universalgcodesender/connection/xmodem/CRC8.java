package com.willwinder.universalgcodesender.connection.xmodem;

/**
 * Created by asirotinkin on 11.11.2014.
 * <p>
 * Original work from here: https://github.com/aesirot/ymodem
 */
public class CRC8 implements CRC {
    @Override
    public int getCRCLength() {
        return 1;
    }

    @Override
    public long calcCRC(byte[] block) {
        byte checksum = 0;
        for (byte b : block) {
            checksum += b;
        }
        return checksum;
    }

}
