package com.willwinder.ugs.designer.logic.io;

import com.willwinder.ugs.designer.logic.Controller;

import java.io.File;

public interface Reader {
    void open(File file, Controller controller);
}
