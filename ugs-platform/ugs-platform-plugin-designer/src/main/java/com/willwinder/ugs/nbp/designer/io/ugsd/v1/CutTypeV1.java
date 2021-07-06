package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;

public enum CutTypeV1 {
    NONE,
    POCKET,
    ON_PATH,
    INSIDE_PATH,
    OUTSIDE_PATH;

    public static CutTypeV1 fromCutType(CutType cutType) {
        if (cutType == CutType.POCKET) {
            return POCKET;
        } else if (cutType == CutType.ON_PATH) {
            return ON_PATH;
        } else if (cutType == CutType.INSIDE_PATH) {
            return INSIDE_PATH;
        } else if (cutType == CutType.OUTSIDE_PATH) {
            return OUTSIDE_PATH;
        } else {
            return NONE;
        }
    }

    public static CutType toCutType(CutTypeV1 cutType) {
        if (cutType == POCKET) {
            return CutType.POCKET;
        } else if (cutType == ON_PATH) {
            return CutType.ON_PATH;
        } else if (cutType == INSIDE_PATH) {
            return CutType.INSIDE_PATH;
        } else if (cutType == OUTSIDE_PATH) {
            return CutType.OUTSIDE_PATH;
        } else {
            return CutType.NONE;
        }
    }
}

