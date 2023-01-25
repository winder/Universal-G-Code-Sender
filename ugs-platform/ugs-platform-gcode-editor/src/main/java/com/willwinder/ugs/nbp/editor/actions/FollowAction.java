/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;

import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * Sets if the editor should follow the currently executed gcode.
 *
 * @author Joacim Breiler
 */
@ActionID(category = LocalizingService.CATEGORY_PROGRAM, id = "FollowAction")
@ActionRegistration(iconBase = FollowAction.ICON_BASE, displayName = "Follow", lazy = false)
public class FollowAction extends AbstractEditorAction implements UGSEventListener, PreferenceChangeListener, Presenter.Toolbar {
    public static final String PREFERENCE_KEY = "follow-action";
    public static final String NAME = "follow-action";
    public static final String ICON_BASE = "icons/follow.svg";

    public FollowAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Follow");
        putValue(Action.NAME, NAME);
        putValue(Action.SHORT_DESCRIPTION, "Follow the running gcode");
        setEnabled(isEnabled());
        registerListeners();
    }

    @EditorActionRegistration(
            name = FollowAction.NAME,
            toolBarPosition = 14,
            mimeType = GcodeLanguageConfig.MIME_TYPE,
            iconResource = FollowAction.ICON_BASE)
    public static FollowAction create(Map<String,?> attrs) {
        return new FollowAction();
    }

    private void registerListeners() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        setEnabled(isEnabled());

        Preferences preferences = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        preferences.addPreferenceChangeListener(this);
        setSelected(preferences.getBoolean(PREFERENCE_KEY, false));
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof ControllerStateEvent || cse instanceof FileStateEvent) {
            EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return CentralLookup.getDefault().lookup(BackendAPI.class).getGcodeFile() != null;
    }

    @Override
    protected void actionPerformed(ActionEvent evt, JTextComponent component) {
        Preferences preferences = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        boolean previousValue = preferences.getBoolean(PREFERENCE_KEY, false);
        preferences.putBoolean(PREFERENCE_KEY, !previousValue);
        setSelected(!previousValue);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent event) {
        if (event != null && event.getKey() != null && event.getKey().equalsIgnoreCase(PREFERENCE_KEY)) {
            setSelected(Boolean.parseBoolean(event.getNewValue()));
        }
    }

    private void setSelected(boolean isSelected) {
        putValue(SELECTED_KEY, isSelected);
        putValue(Actions.ACTION_VALUE_TOGGLE, isSelected);
    }

    @Override
    public Component getToolbarPresenter() {
        JToggleButton toggleButton = new JToggleButton(this);
        toggleButton.putClientProperty("hideActionText", Boolean.TRUE);
        return toggleButton;
    }
}
