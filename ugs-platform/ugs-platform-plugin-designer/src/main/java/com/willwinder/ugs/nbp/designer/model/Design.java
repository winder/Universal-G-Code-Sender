package com.willwinder.ugs.nbp.designer.model;

import com.willwinder.ugs.nbp.designer.entities.Entity;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class Design {
    private File file;

    private List<Entity> entities;

    private Settings settings;

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Optional<File> getFile() {
        return Optional.ofNullable(file);
    }
}
