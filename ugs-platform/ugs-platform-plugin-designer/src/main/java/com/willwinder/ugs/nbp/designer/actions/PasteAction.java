package com.willwinder.ugs.nbp.designer.actions;

import com.google.gson.JsonSyntaxException;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.PasteAction",
        category = "Edit")
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "D-V")
})
public class PasteAction extends AbstractAction {

    private static final Logger LOGGER = Logger.getLogger(PasteAction.class.getSimpleName());
    private final Controller controller;

    public PasteAction(Controller controller) {
        putValue("menuText", "Paste");
        putValue(NAME, "Paste");
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            String data = (String) contents.getTransferData(DataFlavor.stringFlavor);
            UgsDesignReader reader = new UgsDesignReader();
            List<Entity> entities = reader.deserialize(data);
            controller.getDrawing().insertEntities(entities);
            controller.getSelectionManager().clearSelection();
            controller.getSelectionManager().setSelection(entities);
        } catch (UnsupportedFlavorException | JsonSyntaxException | IOException ex) {
            LOGGER.log(Level.INFO, "Unknown paste buffer data format, ignoring");
        }
    }
}
