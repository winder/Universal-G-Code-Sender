package com.willwinder.ugs.designer.io.ugsd.v1;

import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.io.ugsd.common.UgsDesign;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;

public class DesignV1 extends UgsDesign implements Serializable {
    public static final String VERSION = "1";

    private List<EntityV1> entities = Collections.emptyList();
    private ToolDefinition tool;
    private SettingsV1 settings;
    private List<ConstraintV1> constraints;

    public DesignV1() {
        setVersion(DesignV1.VERSION);
    }

    public List<EntityV1> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityV1> entities) {
        this.entities = entities;
    }

    public List<ConstraintV1> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintV1> constraints) {
        this.constraints = constraints;
    }

    public ToolDefinition getTool() {
        return tool;
    }

    public void setTool(ToolDefinition tool) {
        this.tool = tool;
    }

    public SettingsV1 getSettings() {
        return settings;
    }

    public void setSettings(SettingsV1 settings) {
        this.settings = settings;
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
            design.setConstraints(convertConstraints());
        }
        design.setToolSnapshot(tool);
        if (settings != null) {
            design.setSettings(settings.toInternal());
        }
        return design;
    }

    private List<Constraint> convertConstraints() {
        if (constraints == null) {
            return Collections.emptyList();
        }
        Map<String, Entity> entitiesById = new HashMap<>();
        entities.forEach(entity -> collectIds(entity, entitiesById));
        return constraints.stream()
                .filter(Objects::nonNull)
                .map(constraint -> constraint.toInternal(entitiesById))
                .flatMap(Optional::stream)
                .toList();
    }

    private static void collectIds(EntityV1 entity, Map<String, Entity> entitiesById) {
        if (entity == null) {
            return;
        }
        if (entity.getId() != null && entity.getInternal() != null) {
            entitiesById.put(entity.getId(), entity.getInternal());
        }
        if (entity instanceof EntityGroupV1 group && group.getChildren() != null) {
            group.getChildren().forEach(child -> collectIds(child, entitiesById));
        }
    }
}
