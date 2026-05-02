package com.willwinder.ugs.designer.io.ugsd.v1;

import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.io.ugsd.common.UgsDesign;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class DesignV1 extends UgsDesign implements Serializable {
    public static final String VERSION = "1";

    private List<EntityV1> entities = Collections.emptyList();
    private ToolDefinition tool;

    public DesignV1() {
        setVersion(DesignV1.VERSION);
    }

    public List<EntityV1> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityV1> entities) {
        this.entities = entities;
    }

    public ToolDefinition getTool() {
        return tool;
    }

    public void setTool(ToolDefinition tool) {
        this.tool = tool;
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
        design.setToolSnapshot(tool);
        return design;
    }
}
