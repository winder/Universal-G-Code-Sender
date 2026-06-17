package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;

import java.io.File;

public interface WorkspaceOpenStrategy {
    boolean supports(File file);
    WorkspaceContext open(File file);
}
