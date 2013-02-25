/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.CLI;

import com.willwinder.universalgcodesender.visualizer.VisualizerWindow;

/**
 *
 * @author wwinder
 */
public class VisualizerCLI {
    public static void main(String args[]) {
        VisualizerWindow vw = new VisualizerWindow();
        if (args.length > 0) {
            vw.setGcodeFile(args[0]);
        }
        
        vw.setVisible(true);
    }
}
