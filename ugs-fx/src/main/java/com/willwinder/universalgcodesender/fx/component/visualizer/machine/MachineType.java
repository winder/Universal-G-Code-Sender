package com.willwinder.universalgcodesender.fx.component.visualizer.machine;

import java.util.Arrays;

public enum MachineType {
    GENMITSU_PRO_MAX("Genmitsu Pro Max"),
    LONGMILL("Long Mill"),
    UNKNOWN("Unknown");

    private final String name;

    MachineType(String name) {
        this.name = name;
    }

    public static MachineType fromName(String name) {
        return Arrays.stream(values())
                .filter(t -> t.name.equals(name))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static MachineType fromValue(String value) {
        return Arrays.stream(values())
                .filter(t -> t.name().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public String getName() {
        return name;
    }
}
