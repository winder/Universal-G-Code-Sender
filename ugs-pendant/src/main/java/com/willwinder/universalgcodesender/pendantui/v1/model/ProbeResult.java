package com.willwinder.universalgcodesender.pendantui.v1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.willwinder.universalgcodesender.model.Position;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProbeResult implements Serializable {
    private boolean success;
    private String message;
    private Position probedPosition;

    public static ProbeResult ok(Position probedPosition) {
        ProbeResult result = new ProbeResult();
        result.success = true;
        result.probedPosition = probedPosition;
        return result;
    }

    public static ProbeResult failed(String message) {
        ProbeResult result = new ProbeResult();
        result.success = false;
        result.message = message;
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Position getProbedPosition() {
        return probedPosition;
    }

    public void setProbedPosition(Position probedPosition) {
        this.probedPosition = probedPosition;
    }
}
