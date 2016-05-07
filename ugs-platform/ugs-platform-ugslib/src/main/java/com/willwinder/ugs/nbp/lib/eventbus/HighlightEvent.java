/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.lib.eventbus;

import java.util.Collection;

/**
 *
 * @author wwinder
 */
public class HighlightEvent {
    Collection<Integer> lines;
    public HighlightEvent(Collection<Integer> lines) {
        this.lines = lines;
    }    

    public Collection<Integer> getLines() {
        return lines;
    }
}
