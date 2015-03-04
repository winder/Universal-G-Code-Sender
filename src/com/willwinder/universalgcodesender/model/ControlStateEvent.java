/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.model.Utils.ControlState;

/**
 *
 * @author will
 */
public class ControlStateEvent {
    public enum event {
        STATE_CHANGED
    }
    
    event evt = null;
    ControlState controlState = null;
    
    public ControlStateEvent(ControlState state) {
        evt = event.STATE_CHANGED;
        controlState = state;
    }
    
    public event getEventType() {
        return evt;
    }
    
    public ControlState getState() {
        return controlState;
    }
}
