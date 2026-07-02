/*
    Copyright 2026 Will Winder

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
package com.willwinder.ugs.nbp.lib.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ActionRegistrationServiceTest {
    private static final String CATEGORY = "TestActionRegistrationService";
    private static final String MENU_PATH = "Menu/TestActionRegistrationService";
    private static final String LOCAL_MENU_PATH = "Menu/Test Action Registration Service";
    private static final String SHORTCUT = "D-RIGHT";
    private static final String OTHER_SHORTCUT = "D-LEFT";
    private static final String NO_SHORTCUT = "D-UP";

    private ActionRegistrationService service;

    @Before
    public void setUp() throws IOException {
        cleanUp();
        service = new ActionRegistrationService();
    }

    @After
    public void tearDown() throws IOException {
        cleanUp();
    }

    @Test
    public void registerActionCreatesActionAndShortcutForNewAction() throws IOException {
        registerAction("test-new-action", SHORTCUT);

        assertNotNull(getAction("test-new-action"));
        assertNotNull(getShortcut(SHORTCUT));
    }

    @Test
    public void registerActionDoesNotRecreateRemovedShortcutForExistingAction() throws IOException {
        registerAction("test-existing-action", SHORTCUT);
        getShortcut(SHORTCUT).delete();

        registerAction("test-existing-action", SHORTCUT);

        assertNotNull(getAction("test-existing-action"));
        assertNotNull(getMenuItem("test-existing-action"));
        assertNull(getShortcut(SHORTCUT));
    }

    @Test
    public void registerActionWithoutShortcutDoesNotCreateShortcut() throws IOException {
        registerAction("test-null-shortcut", null);
        registerAction("test-empty-shortcut", "");

        assertNotNull(getAction("test-null-shortcut"));
        assertNotNull(getAction("test-empty-shortcut"));
        assertNull(getShortcut(NO_SHORTCUT));
    }

    @Test
    public void registerNewActionStillCreatesShortcutAfterAnotherShortcutWasRemoved() throws IOException {
        registerAction("test-existing-action", SHORTCUT);
        getShortcut(SHORTCUT).delete();

        registerAction("test-other-action", OTHER_SHORTCUT);

        assertNull(getShortcut(SHORTCUT));
        assertNotNull(getAction("test-other-action"));
        assertNotNull(getShortcut(OTHER_SHORTCUT));
    }

    private void registerAction(String id, String shortcut) throws IOException {
        service.registerAction(id, id, CATEGORY, shortcut, MENU_PATH, 100, LOCAL_MENU_PATH, createAction());
    }

    private Action createAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                // Not used by these registration tests.
            }
        };
    }

    private FileObject getAction(String id) {
        return FileUtil.getConfigFile("Actions/" + CATEGORY + "/" + id + ".instance");
    }

    private FileObject getMenuItem(String id) {
        return FileUtil.getConfigFile(MENU_PATH + "/" + id + ".shadow");
    }

    private FileObject getShortcut(String shortcut) {
        FileObject keymaps = FileUtil.getConfigFile("Keymaps");
        String currentKeymap = "NetBeans";
        if (keymaps != null && keymaps.getAttribute("currentKeymap") != null) {
            currentKeymap = keymaps.getAttribute("currentKeymap").toString();
        }

        return FileUtil.getConfigFile("Keymaps/" + currentKeymap + "/" + shortcut + ".shadow");
    }

    private void cleanUp() throws IOException {
        deleteIfExists(FileUtil.getConfigFile("Actions/" + CATEGORY));
        deleteIfExists(FileUtil.getConfigFile(MENU_PATH));
        deleteIfExists(getShortcut(SHORTCUT));
        deleteIfExists(getShortcut(OTHER_SHORTCUT));
        deleteIfExists(getShortcut(NO_SHORTCUT));
    }

    private void deleteIfExists(FileObject fileObject) throws IOException {
        if (fileObject != null) {
            fileObject.delete();
        }
    }
}
