package com.willwinder.universalgcodesender.pendantui.v1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.willwinder.universalgcodesender.listeners.AccessoryStates;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.OverridePercents;
import com.willwinder.universalgcodesender.model.Position;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Status implements Serializable {
    private Position machineCoord = Position.ZERO;
    private Position workCoord = Position.ZERO;
    private Double feedSpeed = 0.0;
    private Double spindleSpeed = 0.0;
    private AccessoryStates accessoryStates = AccessoryStates.EMPTY_ACCESSORY_STATE;
    private OverridePercents overrides = OverridePercents.EMTPY_OVERRIDE_PERCENTS;
    // Sourced from the gcode parser's modal state (M7/M8/M9), refreshed via a
    // "$G" query - unlike accessoryStates, this isn't pushed on every status
    // report, so it's only as fresh as the last "$G" response received.
    private boolean floodCoolantOn = false;
    private ControllerState state = ControllerState.DISCONNECTED;
    private long rowCount;
    private long completedRowCount;
    private long remainingRowCount;
    private String fileName;
    private long sendDuration;
    private long sendRemainingDuration;

    public Status() {
    }

    public Position getWorkCoord() {
        return workCoord;
    }

    public void setWorkCoord(Position workCoord) {
        this.workCoord = workCoord;
    }

    public Double getFeedSpeed() {
        return feedSpeed;
    }

    public void setFeedSpeed(Double feedSpeed) {
        this.feedSpeed = feedSpeed;
    }

    public Double getSpindleSpeed() {
        return spindleSpeed;
    }

    public void setSpindleSpeed(Double spindleSpeed) {
        this.spindleSpeed = spindleSpeed;
    }

    public AccessoryStates getAccessoryStates() {
        return accessoryStates;
    }

    public void setAccessoryStates(AccessoryStates accessoryStates) {
        this.accessoryStates = accessoryStates;
    }

    public OverridePercents getOverrides() {
        return overrides;
    }

    public void setOverrides(OverridePercents overrides) {
        this.overrides = overrides;
    }

    public boolean isFloodCoolantOn() {
        return floodCoolantOn;
    }

    public void setFloodCoolantOn(boolean floodCoolantOn) {
        this.floodCoolantOn = floodCoolantOn;
    }

    public ControllerState getState() {
        return state;
    }

    public void setState(ControllerState state) {
        this.state = state;
    }

    public Position getMachineCoord() {
        return machineCoord;
    }

    public void setMachineCoord(Position machineCoord) {
        this.machineCoord = machineCoord;
    }

    public void setRowCount(long rowCount) {
        this.rowCount = rowCount;
    }

    public long getRowCount() {
        return rowCount;
    }

    public void setCompletedRowCount(long completedRowCount) {
        this.completedRowCount = completedRowCount;
    }

    public long getCompletedRowCount() {
        return completedRowCount;
    }

    public void setRemainingRowCount(long remainingRowCount) {
        this.remainingRowCount = remainingRowCount;
    }

    public long getRemainingRowCount() {
        return remainingRowCount;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setSendDuration(long sendDuration) {
        this.sendDuration = sendDuration;
    }

    public long getSendDuration() {
        return sendDuration;
    }

    public void setSendRemainingDuration(long sendRemainingDuration) {
        this.sendRemainingDuration = sendRemainingDuration;
    }

    public long getSendRemainingDuration() {
        return sendRemainingDuration;
    }
}
