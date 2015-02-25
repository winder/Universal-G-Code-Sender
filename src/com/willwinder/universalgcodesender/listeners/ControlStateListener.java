/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.Utils.ControlState;

/**
 *
 * @author will
 */
public interface ControlStateListener {
    public void ControlStateChanged(ControlState newState);
}
