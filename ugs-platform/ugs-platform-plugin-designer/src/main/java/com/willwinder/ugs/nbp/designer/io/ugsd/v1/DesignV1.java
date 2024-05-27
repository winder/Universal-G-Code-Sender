package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.io.ugsd.common.UgsDesign;
import com.willwinder.ugs.nbp.designer.model.Design;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DesignV1 extends UgsDesign implements Serializable {
    public static final String VERSION = "1";

    private List<EntityV1> entities = Collections.emptyList();

    public DesignV1() {
        setVersion(DesignV1.VERSION);
    }

    public List<EntityV1> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityV1> entities) {
        this.entities = entities;
    }

    @Override
    public Design toInternal() {
        Design design = new Design();
        if (entities != null) {
            List<Entity> internalEntities = getEntities().stream()
                    .filter(Objects::nonNull)
                    .map(EntityV1::toInternal)
                    .toList();
            design.setEntities(internalEntities);
        }
        return design;
    }
}
