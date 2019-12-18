package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class PendantUITest {
	private final BackendAPI mockBackend = EasyMock.createStrictMock(BackendAPI.class);
	private final IController mockController = EasyMock.createStrictMock(AbstractController.class);
	private final PendantUI pendantUI = new PendantUI(mockBackend);
	
	@Before
	public void setup(){
        EasyMock.reset(mockBackend, mockController);
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
}
