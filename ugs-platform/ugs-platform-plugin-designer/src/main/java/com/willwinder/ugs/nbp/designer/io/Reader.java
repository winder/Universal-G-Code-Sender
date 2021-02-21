package com.willwinder.ugs.nbp.designer.io;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.io.File;
import java.util.Optional;

public interface Reader {
    Optional<Entity> read(File file);
}
