/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import org.easymock.Capture;
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
     * Test of executeCustomGcode method, of class MacroHelper.
     */
    @Test
    @Ignore
    public void testExecuteCustomGcode() {
        System.out.println("executeCustomGcode");
        String str = "";
        BackendAPI backend = null;
        MacroHelper.executeCustomGcode(str, backend);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of substituteValues method, of class MacroHelper.
     */
    @Test
    public void testSubstituteValues() {
        System.out.println("substituteValues");

        BackendAPI backend = EasyMock.mock(BackendAPI.class);

        EasyMock.reset(backend);
        final Capture<SystemStateBean> capture = EasyMock.newCapture();
        backend.updateSystemState(EasyMock.capture(capture));
        EasyMock.expect(EasyMock.expectLastCall()).andAnswer(() -> {
            capture.getValue().setMachineX("1");
            capture.getValue().setMachineY("2");
            capture.getValue().setMachineZ("3");
            capture.getValue().setWorkX("4");
            capture.getValue().setWorkY("5");
            capture.getValue().setWorkZ("6");
            return null;
        });
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
        final Capture<SystemStateBean> capture = EasyMock.newCapture();
        backend.updateSystemState(EasyMock.capture(capture));
        EasyMock.expect(EasyMock.expectLastCall());
        EasyMock.replay(backend);

        String result = MacroHelper.substituteValues("{prompt|value 1} {prompt|value 2} {prompt|value 3}", backend);
        System.out.println(result);
    }
    
}
