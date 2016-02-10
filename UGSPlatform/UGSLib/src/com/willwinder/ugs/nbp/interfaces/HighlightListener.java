/**
 * Listener for highlight events.
 */
package com.willwinder.ugs.nbp.interfaces;

import java.util.Collection;

/**
 *
 * @author wwinder
 */
public interface HighlightListener {
    public void highlightsChanged(Collection<Integer> lines);
}
