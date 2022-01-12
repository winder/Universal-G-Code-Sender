/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author wwinder
 */
public class MacroHelperTest {

    /**
     * Test of substituteValues method, of class MacroHelper.
     */
    @Test
    public void testSubstituteValues() {
        System.out.println("substituteValues");

        BackendAPI backend = EasyMock.mock(BackendAPI.class);

        EasyMock.reset(backend);

        Position machinePosition = new Position(1, 2, 3, UnitUtils.Units.MM);
        EasyMock.expect(backend.getMachinePosition()).andAnswer(() -> machinePosition);

        Position workPosition = new Position(4, 5, 6, UnitUtils.Units.MM);
        EasyMock.expect(backend.getWorkPosition()).andAnswer(() -> workPosition);

        EasyMock.replay(backend);

        String result = MacroHelper.substituteValues("{machine_x} {machine_y} {machine_z} {work_x} {work_y} {work_z}", backend);
        assertEquals("1 2 3 4 5 6", result);
    }

    @Test
    @Ignore // This test creates a modal dialog.
    public void testSubstitutePrompt() {
        System.out.println("substituteValuesPrompt");

        BackendAPI backend = EasyMock.mock(BackendAPI.class);

        EasyMock.reset(backend);

        Position machinePosition = new Position(1, 2, 3, UnitUtils.Units.MM);
        EasyMock.expect(backend.getMachinePosition()).andAnswer(() -> machinePosition);

        Position workPosition = new Position(4, 5, 7, UnitUtils.Units.MM);
        EasyMock.expect(backend.getWorkPosition()).andAnswer(() -> workPosition);

        EasyMock.replay(backend);

        String result = MacroHelper.substituteValues("{prompt|value 1} {prompt|value 2} {prompt|value 3}", backend);
        System.out.println(result);
    }
    
}
