package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import static org.assertj.core.api.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.invocation.InvocationOnMock;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Joacim Breiler
 */
public class GrblFirmwareSettingsTest {
    private GrblFirmwareSettings target;

    private IController controller;

    @Before
    public void setUp() {
        controller = mock(IController.class);
        target = new GrblFirmwareSettings(controller);
    }

    @Test
    public void settingMessagesShouldBeProcessedAndAdded() {
        assertEquals("The firmware settings should start with zero settings", 0, target.getAllSettings().size());

        // Emulate a settings-message from the controller
        setFirmwareSetting("$0", "10");

        assertEquals(1, target.getAllSettings().size());
        assertTrue(target.getSetting("$0").isPresent());
        assertEquals("10", target.getSetting("$0").get().getValue());
    }

    @Test
    public void settingMessagesShouldNotAddDuplicates() {
        assertEquals("The firmware settings should start with zero settings", 0, target.getAllSettings().size());

        // Emulate a settings-message from the controller
        setFirmwareSetting("$0", "10");
        setFirmwareSetting("$0", "11");

        assertEquals(1, target.getAllSettings().size());
        assertTrue(target.getSetting("$0").isPresent());
        assertEquals("11", target.getSetting("$0").get().getValue());
    }

    @Test
    public void isHomingEnabledShouldBeTrue() {
        // Emulate a settings-message from the controller
        setFirmwareSetting("$22", "1");
        assertTrue(target.isHomingEnabled());
    }

    @Test
    public void isHomingEnabledShouldBeFalse() {
        // Emulate a settings-message from the controller
        setFirmwareSetting("$22", "0");
        assertFalse(target.isHomingEnabled());
    }

    @Test
    public void isHomingEnabledShouldBeFalseIfNotSet() {
        assertFalse(target.isHomingEnabled());
    }

    @Test
    public void getReportingUnitsShouldReturnUnknownIfNotSet() {
        assertEquals(UnitUtils.Units.UNKNOWN, target.getReportingUnits());
    }

    @Test
    public void getReportingUnitsShouldReturnMm() {
        setFirmwareSetting("$13", "0");
        assertEquals(UnitUtils.Units.MM, target.getReportingUnits());
    }

    @Test
    public void getReportingUnitsShouldReturnInch() {
        setFirmwareSetting("$13", "1");
        assertEquals(UnitUtils.Units.INCH, target.getReportingUnits());
    }

    @Test
    public void getReportingUnitsShouldReturnUnkownOnUnknownValues() {
        setFirmwareSetting("$13", "2");
        assertEquals(UnitUtils.Units.UNKNOWN, target.getReportingUnits());
    }

    @Test
    public void settingMessagesShouldBeSentAsEvents() {
        IFirmwareSettingsListener firmwareSettingsListener = mock(IFirmwareSettingsListener.class);
        target.addListener(firmwareSettingsListener);

        ArgumentCaptor<FirmwareSetting> firmwareSettingArgumentCaptor = ArgumentCaptor.forClass(FirmwareSetting.class);
        doNothing().when(firmwareSettingsListener).onUpdatedFirmwareSetting(firmwareSettingArgumentCaptor.capture());

        // Emulate settings messages from the controller
        setFirmwareSetting("$0", "10");
        setFirmwareSetting("$0", "11");

        List<FirmwareSetting> settingUpdates = firmwareSettingArgumentCaptor.getAllValues();
        assertEquals(2, settingUpdates.size());
        assertEquals("10", settingUpdates.get(0).getValue());
        assertEquals("11", settingUpdates.get(1).getValue());
    }

    @Test(expected = FirmwareSettingsException.class)
    public void setValueForSettingThatDoesNotExistShouldThrowException() throws FirmwareSettingsException {
        target.setValue("$0", "10");
    }

    @Test
    public void setValueWithSameValueShouldNotUpdate() throws FirmwareSettingsException {
        // Given
        setFirmwareSetting("$0", "10");

        // When
        IFirmwareSettingsListener firmwareSettingsListener = mock(IFirmwareSettingsListener.class);
        target.addListener(firmwareSettingsListener);
        target.setValue("$0", "10");

        // Then
        verifyNoInteractions(firmwareSettingsListener);
        verifyNoInteractions(controller);
    }

    @Test
    public void setValueShouldUpdateOnController() throws Exception {
        // Given
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$0", "10");

        // Add a listener
        IFirmwareSettingsListener firmwareSettingsListener = mock(IFirmwareSettingsListener.class);
        ArgumentCaptor<FirmwareSetting> firmwareSettingArgumentCaptor = ArgumentCaptor.forClass(FirmwareSetting.class);
        doNothing().when(firmwareSettingsListener).onUpdatedFirmwareSetting(firmwareSettingArgumentCaptor.capture());
        target.addListener(firmwareSettingsListener);

        // When
        // Try to update the value in it's own thread
        Future<?> setValueFuture = Executors.newCachedThreadPool().submit(() -> {
            try {
                return target.setValue("$0", "11");
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
            return null;
        });

        // Simulate the response from the controller
        Thread.sleep(200);
        command.setOk(true);
        command.setDone(true);

        // Wait until the value gets updated
        FirmwareSetting firmwareSetting = (FirmwareSetting) setValueFuture.get();

        // Then
        assertEquals("11", firmwareSetting.getValue());
        verify(controller, times(1)).sendCommandImmediately(any());
        verify(firmwareSettingsListener, times(1)).onUpdatedFirmwareSetting(any());
        assertEquals(1, firmwareSettingArgumentCaptor.getAllValues().size());
        assertEquals("11", firmwareSettingArgumentCaptor.getAllValues().get(0).getValue());
    }

    @Test
    public void setValueShouldNotUpdateOnError() throws Exception {
        // Given
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$0", "10");

        // Add a listener
        IFirmwareSettingsListener firmwareSettingsListener = mock(IFirmwareSettingsListener.class);
        target.addListener(firmwareSettingsListener);

        // When
        // Try to update the value in it's own thread
        Future<?> setValueFuture = Executors.newCachedThreadPool().submit(() -> {
            try {
                return target.setValue("$0", "11");
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
            return null;
        });

        // Simulate the response from the controller
        Thread.sleep(200);
        command.setError(true);
        command.setDone(true);

        // Wait until the value gets updated
        FirmwareSetting setting = (FirmwareSetting) setValueFuture.get();

        // Then
        assertNotNull(setting);
        assertEquals("10", setting.getValue());
        verify(controller, times(1)).sendCommandImmediately(any());
        verify(firmwareSettingsListener, times(0)).onUpdatedFirmwareSetting(any());
    }

    @Test
    public void setValueShouldTimeoutIfNoResponseFromController() throws Exception {
        // Given
        when(controller.createCommand(anyString())).thenAnswer((InvocationOnMock invocation) -> new GcodeCommand(invocation.getArgument(0)));
        setFirmwareSetting("$0", "10");

        // Add a listener
        IFirmwareSettingsListener firmwareSettingsListener = mock(IFirmwareSettingsListener.class);
        target.addListener(firmwareSettingsListener);

        // When / Then
        assertThrows(FirmwareSettingsException.class, () -> target.setValue("$0", "11"));
        verify(controller, times(1)).sendCommandImmediately(any());
        verify(firmwareSettingsListener, times(0)).onUpdatedFirmwareSetting(any());
    }

    @Test
    public void getInvertDirectionShouldReturnEachBitAsAxis() throws FirmwareSettingsException {
        setFirmwareSetting("$3", "0");
        assertFalse(target.isInvertDirection(Axis.X));
        assertFalse(target.isInvertDirection(Axis.Y));
        assertFalse(target.isInvertDirection(Axis.Z));

        setFirmwareSetting("$3", "1");
        assertTrue(target.isInvertDirection(Axis.X));
        assertFalse(target.isInvertDirection(Axis.Y));
        assertFalse(target.isInvertDirection(Axis.Z));

        setFirmwareSetting("$3", "2");
        assertFalse(target.isInvertDirection(Axis.X));
        assertTrue(target.isInvertDirection(Axis.Y));
        assertFalse(target.isInvertDirection(Axis.Z));

        setFirmwareSetting("$3", "4");
        assertFalse(target.isInvertDirection(Axis.X));
        assertFalse(target.isInvertDirection(Axis.Y));
        assertTrue(target.isInvertDirection(Axis.Z));

        setFirmwareSetting("$3", "7");
        assertTrue(target.isInvertDirection(Axis.X));
        assertTrue(target.isInvertDirection(Axis.Y));
        assertTrue(target.isInvertDirection(Axis.Z));
    }

    @Test
    public void setInvertDirectionXToFalseShouldUnsetBit() throws Exception {
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$3", "7");

        // Try setting X to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.X, false);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);

        // Simulate answer from server
        command.setOk(true);
        command.setDone(true);
        Thread.sleep(100);

        assertTrue(target.getSetting("$3").isPresent());
        assertEquals("6", target.getSetting("$3").get().getValue());
    }

    @Test
    public void setInvertDirectionXShouldSetBit() throws Exception {
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$3", "6");

        // Try setting X to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.X, true);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);

        // Simulate answer from server
        command.setOk(true);
        command.setDone(true);
        Thread.sleep(100);

        assertTrue(target.getSetting("$3").isPresent());
        assertEquals("7", target.getSetting("$3").get().getValue());
    }

    @Test
    public void setInvertDirectionYShouldSetBit() throws Exception {
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$3", "7");

        // Try setting Y to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Y, false);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);

        // Simulate answer from server
        command.setOk(true);
        command.setDone(true);
        Thread.sleep(100);

        assertTrue(target.getSetting("$3").isPresent());
        assertEquals("5", target.getSetting("$3").get().getValue());
    }

    @Test
    public void setInvertDirectionYShouldUnsetBit() throws Exception {
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$3", "5");

        // Try setting Y to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Y, true);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);

        // Simulate answer from server
        command.setOk(true);
        command.setDone(true);
        Thread.sleep(100);

        assertTrue(target.getSetting("$3").isPresent());
        assertEquals("7", target.getSetting("$3").get().getValue());
    }

    @Test
    public void setInvertDirectionZShouldSetBit() throws Exception {
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$3", "7");

        // Try setting Z to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Z, false);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);

        // Simulate answer from server
        command.setOk(true);
        command.setDone(true);
        Thread.sleep(100);

        assertTrue(target.getSetting("$3").isPresent());
        assertEquals("3", target.getSetting("$3").get().getValue());
    }


    @Test
    public void setInvertDirectionZShouldUnsetBit() throws Exception {
        GcodeCommand command = new GcodeCommand("");
        when(controller.createCommand(anyString())).thenReturn(command);
        setFirmwareSetting("$3", "3");

        // Try setting Z to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Z, true);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);

        // Simulate answer from server
        command.setOk(true);
        command.setDone(true);
        Thread.sleep(100);

        assertTrue(target.getSetting("$3").isPresent());
        assertEquals("7", target.getSetting("$3").get().getValue());
    }

    @Test
    public void getMaxSpindleSpeedShouldReturnIntegerValue() throws FirmwareSettingsException {
        setFirmwareSetting("$30", "1000.0");
        assertEquals(1000, target.getMaxSpindleSpeed());

        setFirmwareSetting("$30", "1000");
        assertEquals(1000, target.getMaxSpindleSpeed());

        setFirmwareSetting("$30", "1000,00");
        assertEquals(1000, target.getMaxSpindleSpeed());

        setFirmwareSetting("$30", "1000.9");
        assertEquals(1000, target.getMaxSpindleSpeed());
    }

    private void setFirmwareSetting(String key, String value) {
        target.updateFirmwareSetting(new FirmwareSetting(key, value));
    }
}
