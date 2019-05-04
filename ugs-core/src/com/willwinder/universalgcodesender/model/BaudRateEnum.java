package com.willwinder.universalgcodesender.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum BaudRateEnum {
    BAUD_RATE_2400("2400"),
    BAUD_RATE_4800("4800"),
    BAUD_RATE_9600("9600"),
    BAUD_RATE_19200("19200"),
    BAUD_RATE_38400("38400"),
    BAUD_RATE_57600("57600"),
    BAUD_RATE_115200("115200"),
    BAUD_RATE_230400("230400");

    private final String baudRate;

    BaudRateEnum(String baudRate) {
        this.baudRate = baudRate;
    }

    public String getBaudRate() {
        return baudRate;
    }

    public static String[] getAllBaudRates() {
        return Arrays.stream(values())
                .map(BaudRateEnum::getBaudRate)
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }
}
