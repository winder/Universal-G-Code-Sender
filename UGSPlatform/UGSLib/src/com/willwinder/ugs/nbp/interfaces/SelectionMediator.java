/**
 * This class is a mediator for editor selection events. It is registered at
 * load time so that any editors can send their selection events to it, and any
 * visualizers can listen for selection events, but neither must exist.
 */
package com.willwinder.ugs.nbp.interfaces;

import com.willwinder.ugs.nbp.interfaces.EditorListener;
import com.willwinder.ugs.nbp.interfaces.HighlightListener;

/**
 *
 * @author wwinder
 */
public class SelectionMediator {
    private static SelectionMediator singleton = new SelectionMediator();

    public static SelectionMediator getSelectionMediator() {
        return singleton;
    }

    EditorListener el = null;
    HighlightListener hl = null;

    public void setEditorListener(EditorListener el) {
        this.el = el;
        resolveLink();
    }

    public void setHighlightListener(HighlightListener hl) {
        this.hl = hl;
        resolveLink();
    }
    
    private void resolveLink() {
        if (el != null && hl != null) {
            el.setHighlightListener(hl);
        }
    }
}
