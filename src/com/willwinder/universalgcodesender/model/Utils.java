/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.model;

/**
 *
 * @author will
 */
public class Utils {
    public enum Units {
        MM,
        INCH,
        UNKNOWN
    };
    
    public enum ControlState {
        COMM_DISCONNECTED,
        COMM_IDLE,
        COMM_SENDING,
        COMM_SENDING_PAUSED,
    };
    
}
