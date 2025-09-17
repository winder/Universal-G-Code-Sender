package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.model.MacroAdapter;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;
import javafx.collections.ObservableList;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

public class MacroRegistryTest {

    @Test
    public void getMacrosShouldReturnMacrosFromSettings() {
        BackendAPI backend = mock(BackendAPI.class);
        Settings settings = new Settings();
        settings.setMacros(List.of(
                new Macro("1", "name1", "desc1", "gcode1"),
                new Macro("2", "name2", "desc2", "gcode2")));
        when(backend.getSettings()).thenReturn(settings);

        MacroRegistry registry = new MacroRegistry(backend);
        ObservableList<MacroAdapter> result = registry.getMacros();

        assertEquals(2, result.size());
        assertEquals(settings.getMacros().get(0), result.get(0).getMacro());
        assertEquals(settings.getMacros().get(1), result.get(1).getMacro());
    }

    @Test
    public void getMacrosShouldReturnChangedMacrosFromSettings() {
        BackendAPI backend = mock(BackendAPI.class);
        Settings settings = new Settings();
        settings.setMacros(List.of(
                new Macro("1", "name1", "desc1", "gcode1"),
                new Macro("2", "name2", "desc2", "gcode2")));
        when(backend.getSettings()).thenReturn(settings);

        MacroRegistry registry = new MacroRegistry(backend);
        Macro newMacro = new Macro("3", "name3", "desc3", "gcode3");
        settings.setMacros(List.of(newMacro));
        ObservableList<MacroAdapter> result = registry.getMacros();

        assertEquals(1, result.size());
        assertEquals(newMacro, result.get(0).getMacro());
    }

    @Test
    public void whenMacroIsRemovedShouldUpdateSettings() {
        BackendAPI backend = mock(BackendAPI.class);
        Settings settings = new Settings();
        settings.setMacros(List.of(
                new Macro("1", "name1", "desc1", "gcode1"),
                new Macro("2", "name2", "desc2", "gcode2")));
        when(backend.getSettings()).thenReturn(settings);

        MacroRegistry registry = new MacroRegistry(backend);
        MacroAdapter firstMacro = registry.getMacros().stream().filter(m -> m.getMacro().getUuid().equals("1")).findFirst().orElseThrow();
        registry.getMacros().remove(firstMacro);

        ObservableList<MacroAdapter> result = registry.getMacros();
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).getMacro().getUuid());
    }

}