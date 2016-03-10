/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.control;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Utils.Units;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=JogService.class) 
public class JogService {
    private double stepSize;
    private Units units;

    BackendAPI backend;

    public JogService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    }

    public void setStepSize(double size) {
        this.stepSize = size;
    }

    public void setUnits(Units units) {
        this.units = units;
    }
    
    public void adjustManualLocation(int x, int y, int z) {
        try {
            this.backend.adjustManualLocation(x, y, z, stepSize, units);
        } catch (Exception e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}
