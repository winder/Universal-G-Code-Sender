package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.SaveAsCapable;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class UgsSaveCookie implements Icon, SaveCookie {
    private final UgsDataObject dataObject;
    private final Icon icon = ImageUtilities.loadImageIcon("img/new.svg", false);

    public UgsSaveCookie(UgsDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public void save() {
        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        if (controller == null) {
            throw new IllegalStateException("Couldn't find an instance of the drawing controller");
        }

        try {
            boolean valid = dataObject.getPrimaryFile().getFileSystem().isValid();

        } catch (FileStateInvalidException e) {
            e.printStackTrace();
        }

        UgsDesignWriter writer = new UgsDesignWriter();
        writer.write(new File(dataObject.getPrimaryFile().getPath()), controller);
        dataObject.setModified(false);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
