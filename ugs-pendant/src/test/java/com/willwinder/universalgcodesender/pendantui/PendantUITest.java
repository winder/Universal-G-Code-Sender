package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PendantUITest {
	private final BackendAPI mockBackend = EasyMock.createStrictMock(BackendAPI.class);
	private final IController mockController = EasyMock.createStrictMock(AbstractController.class);
	private final SystemStateBean systemState = new SystemStateBean();
	private final PendantUI pendantUI = new PendantUI(mockBackend);
	
	@Before
	public void setup(){
        EasyMock.reset(mockBackend, mockController);
        pendantUI.setSystemState(systemState);
	}
	@Test
	public void testPendantUI() {
            assertSame(mockBackend, pendantUI.getMainWindow());
	}

	@Test
	public void testGetUrl() {
        String test = pendantUI.getUrlList().get(0).getUrlString();

        assertTrue(test.startsWith("http://"));
        assertTrue(test.contains("8080"));
	}

	@Test
	public void testGetPort() {
        pendantUI.setPort(999);
        assertEquals(999, pendantUI.getPort());
	}

	@Test
	public void testSetPort() {
        pendantUI.setPort(999);
        assertEquals(999, pendantUI.getPort());
	}

	@Test
	public void testIsManualControlEnabled() {
        systemState.setControlState(ControlState.COMM_DISCONNECTED);
        assertFalse(pendantUI.isManualControlEnabled());

        systemState.setControlState(ControlState.COMM_IDLE);
        assertTrue(pendantUI.isManualControlEnabled());

        systemState.setControlState(ControlState.COMM_SENDING);
        assertFalse(pendantUI.isManualControlEnabled());

        systemState.setControlState(ControlState.COMM_SENDING_PAUSED);
        assertTrue(pendantUI.isManualControlEnabled());

        //systemState.setControlState(ControlState.FILE_SELECTED);
        //assertTrue(pendantUI.isManualControlEnabled());
	}
	
	@Test
	public void testSystemStateSetFileName(){
        String fileSeparator = System.getProperty("file.separator");

        String testFileName = fileSeparator+"folder1"+fileSeparator+"folder2"+fileSeparator+"fileName.nc";

        systemState.setFileName(testFileName);

        assertEquals("fileName.nc", systemState.getFileName());
	}
}
