package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.netbeans.api.editor.EditorRegistry;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

@ActionID(
        category = LocalizingService.CATEGORY_EDIT,
        id = "com.willwinder.ugs.nbp.editor.actions.InsertPositionAction")
@ActionRegistration(
        iconBase = InsertPositionAction.SMALL_ICON_PATH,
        displayName = "Insert position",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_EDIT,
                position = 1901,
                separatorAfter = 1999),
})
public class InsertPositionAction extends AbstractAction implements UGSEventListener {
    public static final String NAME = LocalizingService.InsertPositionTitle;
    public static final String SMALL_ICON_PATH = "icons/position.svg";
    private static final String LARGE_ICON_PATH = "icons/position24.svg";
    public static NumberFormat formatter = new DecimalFormat("#.###", Localization.dfs);
    private final transient BackendAPI backend;

    public InsertPositionAction() {
        putValue("menuText", NAME);
        putValue(Action.NAME, NAME);
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof ControllerStateEvent || cse instanceof FileStateEvent) {
            EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend.getGcodeFile() != null &&
                backend.isConnected() &&
                !backend.isSendingFile() &&
                EditorRegistry.lastFocusedComponent() != null &&
                EditorRegistry.lastFocusedComponent().getDocument() != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) {
            return;
        }

        try {
            Document document = EditorRegistry.lastFocusedComponent().getDocument();
            int caretPosition = EditorRegistry.lastFocusedComponent().getCaretPosition();
            document.insertString(caretPosition, getPositionAsString() + "\n", null);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getPositionAsString() {
        Position workPosition = backend.getWorkPosition();
        StringBuilder position = new StringBuilder();
        for (Axis axis : Axis.values()) {
            if (!Double.isNaN(workPosition.get(axis))) {
                position.append(axis.name()).append(formatter.format(workPosition.get(axis))).append(" ");
            }
        }
        return StringUtils.trimToEmpty(position.toString());
    }
}
