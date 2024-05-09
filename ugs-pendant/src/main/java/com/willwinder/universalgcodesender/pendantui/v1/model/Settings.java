package com.willwinder.universalgcodesender.pendantui.v1.model;

import com.willwinder.universalgcodesender.model.UnitUtils;

import java.io.Serializable;

public class Settings implements Serializable {

    private double jogFeedRate;
    private double jogStepSizeXY;
    private UnitUtils.Units preferredUnits;
    private double jogStepSizeZ;
    private String port;
    private String portRate;
    private String firmwareVersion;
    private boolean useZStepSize;

    public void setJogFeedRate(double jogFeedRate) {
        this.jogFeedRate = jogFeedRate;
    }

    public double getJogFeedRate() {
        return jogFeedRate;
    }

    public void setJogStepSizeXY(double jogStepSizeXY) {
        this.jogStepSizeXY = jogStepSizeXY;
    }

    public double getJogStepSizeXY() {
        return jogStepSizeXY;
    }

    public void setPreferredUnits(UnitUtils.Units preferredUnits) {
        this.preferredUnits = preferredUnits;
    }

    public UnitUtils.Units getPreferredUnits() {
        return preferredUnits;
    }

    public double getJogStepSizeZ() {
        return jogStepSizeZ;
    }

    public void setJogStepSizeZ(double jogStepSizeZ) {
        this.jogStepSizeZ = jogStepSizeZ;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    public void setPortRate(String portRate) {
        this.portRate = portRate;
    }

    public String getPortRate() {
        return portRate;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setUseZStepSize(boolean useZStepSize) {
        this.useZStepSize = useZStepSize;
    }

    public boolean isUseZStepSize() {
        return useZStepSize;
    }
}
