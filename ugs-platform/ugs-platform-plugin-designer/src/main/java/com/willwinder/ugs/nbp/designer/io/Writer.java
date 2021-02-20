package com.willwinder.ugs.nbp.designer.io;

import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.io.File;

public interface Writer {
    void write(File file, Controller controller);
}
