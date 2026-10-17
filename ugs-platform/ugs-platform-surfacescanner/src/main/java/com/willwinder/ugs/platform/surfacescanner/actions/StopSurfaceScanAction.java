/*
    Copyright 2026 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 */
package com.willwinder.ugs.platform.surfacescanner.actions;

import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScannerListener;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.util.NbBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;

/** Stops an active AutoLeveler surface scan and clears its queued commands. */
public class StopSurfaceScanAction extends AbstractAction implements SurfaceScannerListener {
    private final SurfaceScanner surfaceScanner;

    public StopSurfaceScanAction(SurfaceScanner surfaceScanner) {
        this.surfaceScanner = surfaceScanner;
        String title = NbBundle.getMessage(SurfaceScanner.class, "StopScan");
        putValue(NAME, title);
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(SurfaceScanner.class, "StopScanTooltip"));
        surfaceScanner.addListener(this);
        setEnabled(surfaceScanner.isScanning() && !surfaceScanner.isStopping());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            surfaceScanner.abort();
        } catch (RuntimeException ex) {
            GUIHelpers.displayErrorDialog(NbBundle.getMessage(SurfaceScanner.class, "StopScanFailed")
                    + ": " + ex.getMessage());
        }
    }

    @Override
    public void onScannerUpdate() {
        SwingUtilities.invokeLater(() -> setEnabled(surfaceScanner.isScanning() && !surfaceScanner.isStopping()));
    }
}
