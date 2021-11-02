package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PasteAction extends AbstractAction {

    private final Controller controller;

    public PasteAction(Controller controller) {
        putValue("menuText", "Paste");
        putValue(NAME, "Paste");

        this.controller = controller;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        /*List<Entity> entities = controller.getCopyPasteBuffer().getEntities();
        List<Entity> cloned = entities.stream()
                .filter(entity -> entity instanceof Cuttable)
                .map(entity -> (Cuttable) entity)
                .map(Cuttable::cloneCuttable)
                .map(entity -> (Entity) entity)
                .collect(Collectors.toList());

        cloned.forEach(c -> controller.getDrawing().insertEntity(c));
        controller.getSelectionManager().clearSelection();
        controller.getSelectionManager().setSelection(cloned);
*/
    }
}
