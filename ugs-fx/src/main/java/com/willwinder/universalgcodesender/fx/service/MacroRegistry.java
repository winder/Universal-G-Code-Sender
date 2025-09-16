/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.model.MacroAdapter;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This acts as an adapter for the backend settings used to store macros.
 * It will be able to expose them as an observable list and sync them
 * bi-directionally.
 *
 * @author Joacim Breiler
 */
public class MacroRegistry {
    private static MacroRegistry instance;

    private final ObservableList<MacroAdapter> macros = FXCollections.observableArrayList();
    private final BackendAPI backend;
    private final AtomicBoolean updatingMacros = new AtomicBoolean(false);

    protected MacroRegistry() {
        this(CentralLookup.getDefault().lookup(BackendAPI.class));
    }

    protected MacroRegistry(BackendAPI backend) {
        this.backend = backend;
        this.backend.getSettings().addSettingChangeListener(this::updateMacrosFromBackend);
        macros.addListener((ListChangeListener<MacroAdapter>) c -> updateMacrosToBackend());
        updateMacrosFromBackend();
    }

    public static String findUniqueMacroName() {
        return findUniqueMacroName(0);
    }

    private static String findUniqueMacroName(int index) {
        final String macroName = "Macro #" + (index + 1);
        if (getInstance().getMacros().stream().noneMatch(m ->
                {
                    if (m.getName() != null) {
                        return m.getName().equalsIgnoreCase(macroName);
                    }
                    return false;
                }
        )) {
            return macroName;
        }
        return findUniqueMacroName(index + 1);
    }

    private void updateMacrosToBackend() {
        if (updatingMacros.get()) {
            return;
        }

        backend.getSettings().setMacros(macros.stream().map(MacroAdapter::getMacro).toList());
    }

    private void updateMacrosFromBackend() {
        updatingMacros.set(true);

        // Remove macros no longer present
        this.macros.removeIf(macro -> !backend.getSettings().getMacros().contains(macro.getMacro()));

        // Add new macros
        this.macros.addAll(backend.getSettings().getMacros()
                .stream()
                .filter(macro -> this.macros.stream()
                        .map(MacroAdapter::getMacro)
                        .noneMatch(m -> m.equals(macro)))
                .map(MacroAdapter::new)
                .toList());

        updatingMacros.set(false);
    }

    public static MacroRegistry getInstance() {
        if (instance == null) {
            instance = new MacroRegistry();
        }
        return instance;
    }

    public ObservableList<MacroAdapter> getMacros() {
        return macros;
    }

    public void removeMacro(MacroAdapter macroAdapter) {
        macros.remove(macroAdapter);
    }
}
