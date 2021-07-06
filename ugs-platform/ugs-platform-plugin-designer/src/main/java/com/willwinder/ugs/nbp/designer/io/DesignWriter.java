package com.willwinder.ugs.nbp.designer.io;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Design;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public interface DesignWriter {
    void write(File file, Controller controller);

    void write(OutputStream outputStream, Controller controller);
}
