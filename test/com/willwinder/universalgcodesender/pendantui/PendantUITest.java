package com.willwinder.universalgcodesender.pendantui;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

import com.willwinder.universalgcodesender.MainWindow.ControlState;
import com.willwinder.universalgcodesender.MainWindowAPI;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.pendantui.PendantConfigBean;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.pendantui.PendantConfigBean.StepSizeOption;

public class PendantUITest {
	private MockMainWindow mainWindow = new MockMainWindow();
	private PendantUI pendantUI = new PendantUI(mainWindow);
	
	public class MockMainWindow implements MainWindowAPI{
		
		public ControlStateListener controlStateListener;
		@Override
		public void registerControlStateListener(ControlStateListener controlStateListener) {
			this.controlStateListener = controlStateListener;
		}

		public String commandText;
		@Override
		public void sendGcodeCommand(String commandText) {
			this.commandText = commandText;
			System.out.println(commandText);
		}
		
		public int dirX, dirY, dirZ; 
		public double stepSize;
		
		@Override
		public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize) {
			this.dirX = dirX;
			this.dirY = dirY;
			this.dirZ = dirZ;
			this.stepSize = stepSize;
			System.out.println("dirX: "+dirX+" dirY: "+dirY+" dirZ: "+dirZ+" stepSize: "+stepSize);
		}
	}
	
	@Test
	public void testPendantUI() {
		assertSame(mainWindow, pendantUI.getMainWindow());
	}

	@Test
	public void testStart() {
		String url = pendantUI.start().get(0).getUrlString();
		
		pendantUI.updateControlsForState(ControlState.COMM_IDLE);
		
		// test resource handler
		String indexPage = getResponse(url);
		assertTrue(indexPage.contains("$(function()"));
		
		// test sendGcode Handler
		String sendGcodeResponse = getResponse(url+"/sendGcode?gCode=MyGcode");
		assertEquals(ControlState.COMM_IDLE.name(), sendGcodeResponse);
		assertEquals(mainWindow.commandText, "MyGcode");
		
		// test adjust manual location handler
		String adjustManualLocationResponse = getResponse(url+"/adjustManualLocation?dirX=1&dirY=2&dirZ=3&stepSize=4.0");
		assertEquals(ControlState.COMM_IDLE.name(), adjustManualLocationResponse);
		assertEquals(1,mainWindow.dirX);
		assertEquals(2,mainWindow.dirY);
		assertEquals(3,mainWindow.dirZ);
		assertEquals(4.0,mainWindow.stepSize,0);
		
		// test get control state handler
		String getControlStateResponse = getResponse(url+"/getControlState");
		assertEquals(ControlState.COMM_IDLE.name(), getControlStateResponse);
		
		pendantUI.updateControlsForState(ControlState.COMM_SENDING);
		getControlStateResponse = getResponse(url+"/getControlState");
		assertEquals(ControlState.COMM_SENDING.name(), getControlStateResponse);
		
		// test config handler
		String configResponse = getResponse(url+"/UGSPendantConfig.json");
		assertTrue(configResponse.contains("shortCutButtonList"));
		
		pendantUI.getConfig().getStepSizeList().add(new StepSizeOption("newStepSizeOptionValue", "newStepSizeOptionLabel", false));
		
		configResponse = getResponse(url+"/UGSPendantConfig.json");
		assertTrue(configResponse.contains("newStepSizeOptionValue"));
		
		pendantUI.stop();
	}
	
	private String getResponse(String urlStr){
		StringBuilder out = new StringBuilder();
		
		try {
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
			    out.append(line);
			 }
			 reader.close();
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
	public void testUpdateControlsForState() {
		assertEquals(ControlState.COMM_DISCONNECTED, pendantUI.getControlState());
		assertFalse(pendantUI.isManualControlEnabled());
		
		pendantUI.updateControlsForState(ControlState.FILE_SELECTED);
		
		assertEquals(ControlState.FILE_SELECTED, pendantUI.getControlState());
		assertTrue(pendantUI.isManualControlEnabled());

		pendantUI.updateControlsForState(ControlState.COMM_DISCONNECTED);
		
		assertEquals(ControlState.COMM_DISCONNECTED, pendantUI.getControlState());
		assertFalse(pendantUI.isManualControlEnabled());

		pendantUI.updateControlsForState(ControlState.COMM_SENDING);
		
		assertEquals(ControlState.COMM_SENDING, pendantUI.getControlState());
		assertFalse(pendantUI.isManualControlEnabled());

		pendantUI.updateControlsForState(ControlState.COMM_SENDING_PAUSED);
		
		assertEquals(ControlState.COMM_SENDING_PAUSED, pendantUI.getControlState());
		assertTrue(pendantUI.isManualControlEnabled());

		pendantUI.updateControlsForState(ControlState.COMM_IDLE);
		
		assertEquals(ControlState.COMM_IDLE, pendantUI.getControlState());
		assertTrue(pendantUI.isManualControlEnabled());

	}

	@Test
	public void testStop() {
		// TODO
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
		
		pendantUI.updateControlsForState(ControlState.COMM_DISCONNECTED);
		assertFalse(pendantUI.isManualControlEnabled());

		pendantUI.updateControlsForState(ControlState.COMM_IDLE);
		assertTrue(pendantUI.isManualControlEnabled());
		
		pendantUI.updateControlsForState(ControlState.COMM_SENDING);
		assertFalse(pendantUI.isManualControlEnabled());
		
		pendantUI.updateControlsForState(ControlState.COMM_SENDING_PAUSED);
		assertTrue(pendantUI.isManualControlEnabled());
		
		pendantUI.updateControlsForState(ControlState.FILE_SELECTED);
		assertTrue(pendantUI.isManualControlEnabled());
		
	}

	@Test
	public void testManualControlEnabledCheckHandler(){
		// TODO
	}
	
	@Test
	public void testSendGcodeHandler(){
		// TODO 
	}
}
