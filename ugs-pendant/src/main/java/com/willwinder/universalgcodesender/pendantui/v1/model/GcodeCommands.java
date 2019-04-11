package com.willwinder.universalgcodesender.pendantui.v1.model;

import java.io.Serializable;

public class GcodeCommands implements Serializable {
    private String commands;

    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }
}
