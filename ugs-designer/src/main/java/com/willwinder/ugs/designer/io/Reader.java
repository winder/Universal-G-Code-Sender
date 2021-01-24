package com.willwinder.ugs.designer.io;

import com.willwinder.ugs.designer.logic.Controller;

import java.io.File;

public interface Reader {
    void open(File file, Controller controller);
}
