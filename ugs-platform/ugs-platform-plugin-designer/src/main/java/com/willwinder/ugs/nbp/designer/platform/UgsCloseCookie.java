package com.willwinder.ugs.nbp.designer.platform;

import org.openide.cookies.CloseCookie;
import org.openide.cookies.SaveCookie;
import org.openide.windows.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class UgsCloseCookie implements CloseCookie {
    private final UgsDataObject dataObject;

    public UgsCloseCookie(UgsDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public boolean close() {
        if (!dataObject.isModified()) {
            return true;
        }

        final Component parent = WindowManager.getDefault().getMainWindow();
        final Object[] options = new Object[]{"Save", "Discard", "Cancel"};
        final String message = "File " + dataObject.getName() + " is modified. Save?";
        final int choice = JOptionPane.showOptionDialog(parent, message, "Question", JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, JOptionPane.YES_OPTION);

        boolean canClose = false;
        if (choice == JOptionPane.NO_OPTION) {
            dataObject.setModified(false);
            canClose = true;
        } else if (choice == JOptionPane.YES_OPTION) {
            SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
            if( saveCookie != null) {
                try {
                    saveCookie.save();
                    canClose = true;
                } catch (IOException e) {
                    // Never mind
                }
            }
        }

        return canClose;
    }
}
