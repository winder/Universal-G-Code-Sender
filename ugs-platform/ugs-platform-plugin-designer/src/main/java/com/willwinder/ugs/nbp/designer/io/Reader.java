package com.willwinder.ugs.nbp.designer.io;

import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.io.File;

public interface Reader {
    void open(File file, Controller controller);
}
