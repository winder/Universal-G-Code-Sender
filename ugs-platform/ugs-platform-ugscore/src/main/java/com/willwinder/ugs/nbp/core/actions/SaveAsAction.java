/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A replacement for the built-in SaveAsAction that lets the active
 * {@link SaveAsCapable} document contribute selectable file formats through
 * {@link SaveAsFileFilterProvider}. Documents that do not implement that interface behave exactly
 * like the platform default.
 *
 * @author Joacim Breiler
 */
@ActionID(category = "File", id = "com.willwinder.ugs.nbp.core.actions.SaveAsAction")
@ActionRegistration(displayName = "Save As...", lazy = false)
@ActionReference(path = LocalizingService.MENU_FILE, position = 1600)
public final class SaveAsAction extends AbstractAction implements ContextAwareAction {

    private final Lookup context;
    private final boolean isGlobal;
    private Lookup.Result<SaveAsCapable> lkpInfo;
    private boolean isDirty = true;
    private PropertyChangeListener registryListener;
    private LookupListener lookupListener;

    public SaveAsAction() {
        this(Utilities.actionsGlobalContext(), true);
    }

    private SaveAsAction(Lookup context, boolean isGlobal) {
        super("Save As...");
        this.context = context;
        this.isGlobal = isGlobal;
        putValue("noIconInMenu", Boolean.TRUE);
        setEnabled(false);
    }

    @Override
    public boolean isEnabled() {
        if (isDirty || getPropertyChangeListeners().length == 0) {
            refreshEnabled();
        }
        return super.isEnabled();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        refreshListeners();
        Collection<? extends SaveAsCapable> inst = lkpInfo.allInstances();
        if (inst.isEmpty()) {
            return;
        }

        SaveAsCapable saveAs = inst.iterator().next();
        File newFile = getNewFileName(saveAs);
        if (null == newFile) {
            return;
        }

        try {
            File targetFolder = newFile.getParentFile();
            if (null == targetFolder) {
                throw new IOException(newFile.getAbsolutePath());
            }
            FileObject newFolder = FileUtil.createFolder(targetFolder);
            saveAs.saveAs(newFolder, newFile.getName());
        } catch (IOException ioE) {
            Exceptions.printStackTrace(ioE);
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ioE);
        }
    }

    private File getNewFileName(SaveAsCapable saveAs) {
        File newFile = null;
        FileObject currentFileObject = getCurrentFileObject();
        if (null != currentFileObject) {
            newFile = FileUtil.toFile(currentFileObject);
            if (null == newFile) {
                newFile = new File(currentFileObject.getNameExt());
            }
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save As");
        chooser.setMultiSelectionEnabled(false);
        applyFileFilters(chooser, saveAs);
        if (null != newFile) {
            chooser.setSelectedFile(newFile);
            FileUtil.preventFileChooserSymlinkTraversal(chooser, newFile.getParentFile());
        }

        File origFile = newFile;
        while (true) {
            if (JFileChooser.APPROVE_OPTION != chooser.showSaveDialog(WindowManager.getDefault().getMainWindow())) {
                return null;
            }

            File selectedFile = applyExtension(chooser.getSelectedFile(), chooser.getFileFilter());
            if (null == selectedFile) {
                return null;
            }

            if (selectedFile.equals(origFile)) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("The selected file is the same as the current file.", NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else if (selectedFile.exists()) {
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation("Are you sure you want to overwrite the file " + selectedFile.getName() + "?", "Overwrite existing file", NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                if (NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd)) {
                    return selectedFile;
                }
            } else {
                return selectedFile;
            }
        }
    }

    private void applyFileFilters(JFileChooser chooser, SaveAsCapable saveAs) {
        if (!(saveAs instanceof SaveAsFileFilterProvider provider)) {
            return;
        }

        java.util.List<FileFilter> fileFilters = provider.getSaveAsFileFilters();
        if (fileFilters == null || fileFilters.isEmpty()) {
            return;
        }

        chooser.setAcceptAllFileFilterUsed(false);
        fileFilters.forEach(chooser::addChoosableFileFilter);
        chooser.setFileFilter(fileFilters.get(0));
    }

    private File applyExtension(File file, FileFilter fileFilter) {
        if (file == null || !(fileFilter instanceof FileNameExtensionFilter extensionFilter)) {
            return file;
        }

        boolean hasExtension = false;
        for (String extension : extensionFilter.getExtensions()) {
            if (StringUtils.endsWithIgnoreCase(file.getName(), "." + extension)) {
                hasExtension = true;
                break;
            }
        }

        if (hasExtension) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + "." + extensionFilter.getExtensions()[0]);
    }

    private FileObject getCurrentFileObject() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (null != tc) {
            DataObject dob = tc.getLookup().lookup(DataObject.class);
            if (null != dob) {
                return dob.getPrimaryFile();
            }
        }
        return null;
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new SaveAsAction(actionContext, false);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        refreshListeners();
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        super.removePropertyChangeListener(listener);
        Mutex.EVENT.readAccess(this::refreshListeners);
    }

    private PropertyChangeListener createRegistryListener() {
        return WeakListeners.propertyChange(evt -> isDirty = true, TopComponent.getRegistry());
    }

    private LookupListener createLookupListener() {
        LookupListener listener = (LookupEvent ev) -> isDirty = true;
        return WeakListeners.create(LookupListener.class, listener, lkpInfo);
    }

    private void refreshEnabled() {
        if (lkpInfo == null) {
            lkpInfo = context.lookup(new Lookup.Template<>(SaveAsCapable.class));
        }

        TopComponent tc = TopComponent.getRegistry().getActivated();
        boolean isEditorWindowActivated = null != tc && WindowManager.getDefault().isEditorTopComponent(tc);
        setEnabled(!lkpInfo.allItems().isEmpty() && isEditorWindowActivated);
        isDirty = false;
    }

    private void refreshListeners() {
        assert SwingUtilities.isEventDispatchThread() : "this shall be called just from AWT thread";

        if (lkpInfo == null) {
            lkpInfo = context.lookup(new Lookup.Template<>(SaveAsCapable.class));
        }

        if (getPropertyChangeListeners().length == 0) {
            if (isGlobal && null != registryListener) {
                TopComponent.getRegistry().removePropertyChangeListener(registryListener);
                registryListener = null;
            }
            if (null != lookupListener) {
                lkpInfo.removeLookupListener(lookupListener);
                lookupListener = null;
            }
        } else {
            if (null == registryListener) {
                registryListener = createRegistryListener();
                TopComponent.getRegistry().addPropertyChangeListener(registryListener);
            }
            if (null == lookupListener) {
                lookupListener = createLookupListener();
                lkpInfo.addLookupListener(lookupListener);
            }
            refreshEnabled();
        }
    }
}
