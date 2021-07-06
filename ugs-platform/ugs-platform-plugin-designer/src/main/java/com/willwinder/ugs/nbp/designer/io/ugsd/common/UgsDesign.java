package com.willwinder.ugs.nbp.designer.io.ugsd.common;

import com.willwinder.ugs.nbp.designer.model.Design;

public class UgsDesign {
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Design toInternal() {
        return new Design();
    }
}
