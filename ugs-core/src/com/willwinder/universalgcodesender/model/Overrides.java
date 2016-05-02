/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.model;

/**
 *
 * @author wwinder
 */
public enum Overrides {
    //CMD_DEBUG_REPORT, // 0x85 // Only when DEBUG enabled, sends debug report in '{}' braces.
    CMD_FEED_OVR_RESET, // 0x90         // Restores feed override value to 100%.
    CMD_FEED_OVR_COARSE_PLUS, // 0x91
    CMD_FEED_OVR_COARSE_MINUS, // 0x92
    CMD_FEED_OVR_FINE_PLUS , // 0x93
    CMD_FEED_OVR_FINE_MINUS , // 0x94
    CMD_RAPID_OVR_RESET, // 0x95        // Restores rapid override value to 100%.
    CMD_RAPID_OVR_MEDIUM, // 0x96
    CMD_RAPID_OVR_LOW, // 0x97
    CMD_SPINDLE_OVR_RESET, // 0x99      // Restores spindle override value to 100%.
    CMD_SPINDLE_OVR_COARSE_PLUS, // 0x9A
    CMD_SPINDLE_OVR_COARSE_MINUS, // 0x9B
    CMD_SPINDLE_OVR_FINE_PLUS, // 0x9C
    CMD_SPINDLE_OVR_FINE_MINUS, // 0x9D
    CMD_SPINDLE_OVR_STOP, // 0x9E
}
