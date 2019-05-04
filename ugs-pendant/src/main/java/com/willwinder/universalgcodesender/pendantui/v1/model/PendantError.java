package com.willwinder.universalgcodesender.pendantui.v1.model;

import java.io.Serializable;

public class PendantError implements Serializable {

    private final String errorMessage;

    public PendantError(Throwable cause) {
        this.errorMessage = cause.getMessage();
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
