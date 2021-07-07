package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.io.ugsd.common.UgsDesign;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class DesignV1 extends UgsDesign implements Serializable {
    public static final String VERSION = "1";

    private SettingsV1 settings;
    private List<EntityV1> entities;

    public DesignV1() {
        setVersion(DesignV1.VERSION);
    }

    public List<EntityV1> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityV1> entities) {
        this.entities = entities;
    }

    public Design toInternal() {
        Design design = new Design();
        if (entities != null) {
            design.setEntities(getEntities().stream()
                    .map(EntityV1::toInternal)
                    .collect(Collectors.toList()));
        }
        design.setSettings(settings.toInternal());
        return design;
    }

    public SettingsV1 getSettings() {
        return settings;
    }

    public void setSettings(SettingsV1 settings) {
        this.settings = settings;
    }
}
