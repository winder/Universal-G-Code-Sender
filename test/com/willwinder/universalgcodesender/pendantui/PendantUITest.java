package com.willwinder.universalgcodesender.pendantui;

import com.google.gson.Gson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Before;
import org.junit.Test;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.pendantui.PendantConfigBean.StepSizeOption;
import com.willwinder.universalgcodesender.utils.Settings;
import org.easymock.EasyMock;

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
	public void testStart() throws Exception {

        // This is what we're about to do...
        // 1. Send a command
        mockBackend.sendGcodeCommand("MyGcode");
        EasyMock.expect(EasyMock.expectLastCall()).once();

        // 2. Commands
        mockBackend.performHomingCycle();
        EasyMock.expect(EasyMock.expectLastCall()).once();

        mockBackend.killAlarmLock();
        EasyMock.expect(EasyMock.expectLastCall()).once();

        mockBackend.toggleCheckMode();
        EasyMock.expect(EasyMock.expectLastCall()).once();

        mockBackend.send();
        EasyMock.expect(EasyMock.expectLastCall()).once();

        // 3. Call some invalid commands which wont reach the backend.

        // 4. Change the mode to sending and call pause/resume and cancel.
        mockBackend.pauseResume();
        EasyMock.expect(EasyMock.expectLastCall()).once();

        mockBackend.cancel();
        EasyMock.expect(EasyMock.expectLastCall()).once();

        // 5. Adjust machine location.
        mockBackend.adjustManualLocation(1, 2, 3, 4.0, Utils.Units.UNKNOWN);
        EasyMock.expect(EasyMock.expectLastCall()).once();

        // 6. Get system state
        mockBackend.updateSystemState(EasyMock.anyObject(SystemStateBean.class));
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        // 7. Get settings
        Settings settings = new Settings();
        settings.getPendantConfig().getStepSizeList().add(new StepSizeOption("newStepSizeOptionValue", "newStepSizeOptionLabel", false));
        EasyMock.expect(mockBackend.getSettings()).andReturn(settings).once();

        ///////////////////////////
        // Start mock and do it! //
        ///////////////////////////
        EasyMock.replay(mockBackend);
        pendantUI.setPort(23123);
        String url = pendantUI.start().get(0).getUrlString();
    
        systemState.setControlState(ControlState.COMM_IDLE);
        pendantUI.setSystemState(systemState);
    
        // test resource handler
        String indexPage = getResponse(url);
        assertTrue(indexPage.contains("$(function()"));

        // 1. Send a command
        getResponse(url+"/sendGcode?gCode=MyGcode");
        // 2. Send commands
        getResponse(url+"/sendGcode?gCode=$H");
        getResponse(url+"/sendGcode?gCode=$X");
        getResponse(url+"/sendGcode?gCode=$C");
        getResponse(url+"/sendGcode?gCode=SEND_FILE");

        // 3. Call some invalid commands which wont reach the backend.
        getResponse(url+"/sendGcode?gCode=PAUSE_RESUME_FILE");
        getResponse(url+"/sendGcode?gCode=CANCEL_FILE");

        // 4. Change the mode to sending and call pause/resume and cancel.
        systemState.setControlState(ControlState.COMM_SENDING);
        getResponse(url+"/sendGcode?gCode=PAUSE_RESUME_FILE");
        getResponse(url+"/sendGcode?gCode=CANCEL_FILE");

        // 5. Adjust machine location.
        systemState.setControlState(ControlState.COMM_IDLE);
        String adjustManualLocationResponse =
                getResponse(url+"/adjustManualLocation?dirX=1&dirY=2&dirZ=3&stepSize=4.0");

        // 6. Get system state
        SystemStateBean systemStateTest = new Gson().fromJson(
                getResponse(url+"/getSystemState"), SystemStateBean.class);
        assertEquals(ControlState.COMM_IDLE, systemStateTest.getControlState());

        systemState.setControlState(ControlState.COMM_SENDING);
        systemStateTest = new Gson().fromJson(
                getResponse(url+"/getSystemState"), SystemStateBean.class);
        assertEquals(ControlState.COMM_SENDING, systemStateTest.getControlState());

        // 7. Get settings
        String configResponse = getResponse(url+"/config");
        assertTrue(configResponse.contains("shortCutButtonList"));
        assertTrue(configResponse.contains("newStepSizeOptionValue"));

        // Wrap up.
        pendantUI.stop();
        assertTrue(pendantUI.getServer().isStopped());

        // Verify that all the EasyMock functions were called.
        EasyMock.verify(mockBackend);
	}
	
	private String getResponse(String urlStr){
        StringBuilder out = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }				
        return out.toString();
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
