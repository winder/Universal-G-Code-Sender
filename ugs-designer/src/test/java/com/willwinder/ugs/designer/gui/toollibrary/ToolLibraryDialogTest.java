package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.universalgcodesender.model.UnitUtils;

public class ToolLibraryDialogTest {
    public static void main(String[] args) {
        ToolLibraryService service = new ToolLibraryService();
        ToolLibraryDialog dialog = new ToolLibraryDialog(null, service, UnitUtils.Units.MM);
        dialog.setDefaultCloseOperation(ToolLibraryDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
}
