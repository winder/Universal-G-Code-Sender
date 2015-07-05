package com.willwinder.universalgcodesender.pendantui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.Utils.ControlState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.pendantui.PendantConfigBean.StepSizeOption;
import java.io.File;

public class PendantUITest {
	private final MockMainWindow mainWindow = new MockMainWindow();
	private final PendantUI pendantUI = new PendantUI(mainWindow);
	private final MockUGSController controller = new MockUGSController();
	private final SystemStateBean systemState = new SystemStateBean();
	
	public class MockMainWindow implements BackendAPI {
		
            public String commandText;
            @Override
            public void sendGcodeCommand(String commandText) {
                this.commandText = commandText;
                System.out.println(commandText);
            }

            public int dirX, dirY, dirZ; 
            public double stepSize;
            public Units units;

            @Override
            public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize, Units units) {
                this.dirX = dirX;
                this.dirY = dirY;
                this.dirZ = dirZ;
                this.units = units;
                this.stepSize = stepSize;
                System.out.println("dirX: "+dirX+" dirY: "+dirY+" dirZ: "+dirZ+" stepSize: "+stepSize+" units: "+units);
            }

            public Settings settings = new Settings();

            @Override
            public Settings getSettings() {
                return settings;
            }

            @Override
            public AbstractController getController() {
                return controller;
            }

            @Override
            public void updateSystemState(SystemStateBean systemStateBean) {

            }

            public boolean sendButtonActionPerformed = false;

            @Override
            public void send() {
                sendButtonActionPerformed = true;
                System.out.println("sendButtonActionPerformed");
            }

            public boolean pauseButtonActionPerformed = false;

            @Override
            public void pauseResume() {
                pauseButtonActionPerformed = true;
                System.out.println("pauseButtonActionPerformed");
            }

            public boolean cancelButtonActionPerformed = false;

            @Override
            public void cancel() {
                cancelButtonActionPerformed = true;
                System.out.println("cancelButtonActionPerformed");
            }

            public boolean returnToZeroButtonActionPerformed = false;

            @Override
            public void returnToZero() {
                returnToZeroButtonActionPerformed = true;
                System.out.println("returnToZeroButtonActionPerformed");
            }

            public boolean resetCoordinatesButtonActionPerformed = false;

            @Override
            public void resetCoordinatesToZero() {
                resetCoordinatesButtonActionPerformed = true;
                System.out.println("resetCoordinatesButtonActionPerformed");
            }

            @Override
            public void resetCoordinateToZero(char coord) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void connect(String firmware, String port, int baudRate) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isConnected() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void disconnect() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void applySettings(Settings settings) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public ControlState getControlState() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public String getPauseResumeText() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isPaused() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean canCancel() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void setFile(File file) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public long getSendDuration() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public long getSendRemainingDuration() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void applySettingsToController(Settings settings, IController controller) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean isSending() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean canPause() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public long getNumRows() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public long getNumSentRows() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public long getNumRemainingRows() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void killAlarmLock() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void performHomingCycle() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void toggleCheckMode() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void issueSoftReset() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void requestParserState() throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public File getFile() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public boolean canSend() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addControlStateListener(ControlStateListener listener) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addControllerListener(ControllerListener listener) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void preprocessAndExportToFile(File f) throws Exception {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
	}
	
	public class MockUGSController extends AbstractController{
            @Override
            protected void statusUpdatesRateValueChanged(int rate) {
            }

            @Override
            protected void statusUpdatesEnabledValueChanged(boolean enabled) {
            }

            @Override
            protected void resumeStreamingEvent() throws IOException {
            }

            @Override
            protected void rawResponseHandler(String response) {
            }

            @Override
            protected void pauseStreamingEvent() throws IOException {
            }

            @Override
            protected void isReadyToSendCommandsEvent() throws Exception {
            }

            @Override
            public long getJobLengthEstimate(Collection<String> jobLines) {
                return 0;
            }

            @Override
            protected void closeCommBeforeEvent() {
            }

            @Override
            protected void closeCommAfterEvent() {
            }

            @Override
            protected void cancelSendBeforeEvent() {
            }

            @Override
            protected void cancelSendAfterEvent() {
            }

            public boolean performHomingCycle = false;

            @Override
            public void performHomingCycle() throws Exception {
                performHomingCycle = true;
            }

            public boolean killAlarmLock = false;
            @Override
            public void killAlarmLock() throws Exception {
                killAlarmLock = true;
            }

            public boolean toggleCheckMode = true;
            @Override
            public void toggleCheckMode() throws Exception {
                toggleCheckMode = true;
            }

            @Override
            public void currentUnits(Units units) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

	}
	
	@Before
	public void setup(){
            pendantUI.setSystemState(systemState);
	}
	@Test
	public void testPendantUI() {
            assertSame(mainWindow, pendantUI.getMainWindow());
	}

	@Test
	public void testStart() {
            pendantUI.setPort(23123);
            String url = pendantUI.start().get(0).getUrlString();
		
            systemState.setControlState(ControlState.COMM_IDLE);
		
            // test resource handler
            String indexPage = getResponse(url);
            assertTrue(indexPage.contains("$(function()"));

            // test sendGcode Handler
            getResponse(url+"/sendGcode?gCode=MyGcode");
            assertEquals(mainWindow.commandText, "MyGcode");

            getResponse(url+"/sendGcode?gCode=$H");
            assertTrue(controller.performHomingCycle);

            getResponse(url+"/sendGcode?gCode=$X");
            assertTrue(controller.killAlarmLock);

            getResponse(url+"/sendGcode?gCode=$C");
            assertTrue(controller.toggleCheckMode);

            getResponse(url+"/sendGcode?gCode=SEND_FILE");
            assertTrue(mainWindow.sendButtonActionPerformed);

            // Disabled when idle
            getResponse(url+"/sendGcode?gCode=PAUSE_RESUME_FILE");
            assertFalse(mainWindow.pauseButtonActionPerformed);

            // Disabled when idle
            getResponse(url+"/sendGcode?gCode=CANCEL_FILE");
            assertFalse(mainWindow.cancelButtonActionPerformed);

            systemState.setControlState(ControlState.COMM_SENDING);

            getResponse(url+"/sendGcode?gCode=PAUSE_RESUME_FILE");
            assertTrue(mainWindow.pauseButtonActionPerformed);

            getResponse(url+"/sendGcode?gCode=CANCEL_FILE");
            assertTrue(mainWindow.cancelButtonActionPerformed);

            systemState.setControlState(ControlState.COMM_IDLE);

            // test adjust manual location handler
            String adjustManualLocationResponse = getResponse(url+"/adjustManualLocation?dirX=1&dirY=2&dirZ=3&stepSize=4.0");
            assertEquals(ControlState.COMM_IDLE.name(), adjustManualLocationResponse);
            assertEquals(1,mainWindow.dirX);
            assertEquals(2,mainWindow.dirY);
            assertEquals(3,mainWindow.dirZ);
            assertEquals(4.0,mainWindow.stepSize,0);

            // test get system state handler
            SystemStateBean systemStateTest = new Gson().fromJson(getResponse(url+"/getSystemState"), SystemStateBean.class);
            assertEquals(ControlState.COMM_IDLE, systemStateTest.getControlState());

            systemState.setControlState(ControlState.COMM_SENDING);
            systemStateTest = new Gson().fromJson(getResponse(url+"/getSystemState"), SystemStateBean.class);
            assertEquals(ControlState.COMM_SENDING, systemStateTest.getControlState());

            
            // test config handler
            String configResponse = getResponse(url+"/config");
            assertTrue(configResponse.contains("shortCutButtonList"));

            pendantUI.getMainWindow().getSettings().getPendantConfig().getStepSizeList().add(new StepSizeOption("newStepSizeOptionValue", "newStepSizeOptionLabel", false));

            configResponse = getResponse(url+"/config");
            assertTrue(configResponse.contains("newStepSizeOptionValue"));

            pendantUI.stop();

            assertTrue(pendantUI.getServer().isStopped());
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
