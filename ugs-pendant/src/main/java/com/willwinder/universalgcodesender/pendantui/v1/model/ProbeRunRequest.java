package com.willwinder.universalgcodesender.pendantui.v1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProbeRunRequest implements Serializable {
    private ProbeOperation operation;

    /**
     * Overrides the configured ProbeSettings.maxTravel for just this run - the dashboard sends
     * this so a user's one-off adjustment on the panel doesn't have to be saved first.
     */
    private Double maxTravel;

    public ProbeOperation getOperation() {
        return operation;
    }

    public void setOperation(ProbeOperation operation) {
        this.operation = operation;
    }

    public Double getMaxTravel() {
        return maxTravel;
    }

    public void setMaxTravel(Double maxTravel) {
        this.maxTravel = maxTravel;
    }
}
