package com.willwinder.ugs.nbp.designer.io;

import com.willwinder.ugs.nbp.designer.model.Design;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

public interface DesignReader {
    Optional<Design> read(File file);

    Optional<Design> read(InputStream resourceAsStream);
}
