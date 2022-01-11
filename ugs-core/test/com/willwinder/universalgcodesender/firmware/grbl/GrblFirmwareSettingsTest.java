package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        target.rawResponseListener("$0=10");

        assertEquals(1, target.getAllSettings().size());
        assertEquals("10", target.getSetting("$0").get().getValue());
    }

    @Test
    public void settingMessagesShouldNotAddDuplicates() {
        assertEquals("The firmware settings should start with zero settings", 0, target.getAllSettings().size());

        // Emulate a settings-message from the controller
        target.rawResponseListener("$0=10");
        target.rawResponseListener("$0=11");

        assertEquals(1, target.getAllSettings().size());
        assertEquals("11", target.getSetting("$0").get().getValue());
    }

    @Test
    public void isHomingEnabledShouldBeTrue() throws InterruptedException, FirmwareSettingsException {
        // Emulate a settings-message from the controller
        target.rawResponseListener("$22=1");
        assertTrue(target.isHomingEnabled());
    }

    @Test
    public void isHomingEnabledShouldBeFalse() throws FirmwareSettingsException {
        // Emulate a settings-message from the controller
        target.rawResponseListener("$22=0");
        assertFalse(target.isHomingEnabled());
    }

    @Test(expected = FirmwareSettingsException.class)
    public void isHomingEnabledShouldBeFalseIfNotSet() throws FirmwareSettingsException {
        assertFalse(target.isHomingEnabled());
    }

    @Test
    public void getReportingUnitsShouldReturnUnknownIfNotSet() {
        assertEquals(UnitUtils.Units.UNKNOWN, target.getReportingUnits());
    }

    @Test
    public void getReportingUnitsShouldReturnMm() {
        target.rawResponseListener("$13=0");
        assertEquals(UnitUtils.Units.MM, target.getReportingUnits());
    }

    @Test
    public void getReportingUnitsShouldReturnInch() {
        target.rawResponseListener("$13=1");
        assertEquals(UnitUtils.Units.INCH, target.getReportingUnits());
    }

    @Test
    public void getReportingUnitsShouldReturnUnkownOnUnknownValues() {
        target.rawResponseListener("$13=2");
        assertEquals(UnitUtils.Units.UNKNOWN, target.getReportingUnits());
    }

    @Test
    public void settingMessagesShouldBeSentAsEvents() {
        IFirmwareSettingsListener firmwareSettingsListener = mock(IFirmwareSettingsListener.class);
        target.addListener(firmwareSettingsListener);

        ArgumentCaptor<FirmwareSetting> firmwareSettingArgumentCaptor = ArgumentCaptor.forClass(FirmwareSetting.class);
        doNothing().when(firmwareSettingsListener).onUpdatedFirmwareSetting(firmwareSettingArgumentCaptor.capture());

        // Emulate settings messages from the controller
        target.rawResponseListener("$0=10");
        target.rawResponseListener("$0=11");

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
        target.rawResponseListener("$0=10");

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
        when(controller.isStreaming()).thenReturn(false);
        when(controller.isCommOpen()).thenReturn(true);
        target.rawResponseListener("$0=10");

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
        target.rawResponseListener("ok");

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
        when(controller.isStreaming()).thenReturn(false);
        when(controller.isCommOpen()).thenReturn(true);
        target.rawResponseListener("$0=10");

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
        target.rawResponseListener("error");

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
        when(controller.isStreaming()).thenReturn(false);
        when(controller.isCommOpen()).thenReturn(true);
        target.rawResponseListener("$0=10");

        // Add a listener
        IFirmwareSettingsListener firmwareSettingsListener = mock(IFirmwareSettingsListener.class);
        target.addListener(firmwareSettingsListener);

        // When
        try {
            target.setValue("$0", "11");
        } catch (FirmwareSettingsException e) {
            assertTrue("Make sure the exception contains the word 'Timeout'", e.getMessage().contains("Timeout"));
        }

        // Then
        verify(controller, times(1)).sendCommandImmediately(any());
        verify(firmwareSettingsListener, times(0)).onUpdatedFirmwareSetting(any());
    }

    @Test
    public void getInvertDirectionShouldReturnEachBitAsAxis() throws FirmwareSettingsException {
        target.rawResponseListener("$3=0");
        assertEquals(false, target.isInvertDirection(Axis.X));
        assertEquals(false, target.isInvertDirection(Axis.Y));
        assertEquals(false, target.isInvertDirection(Axis.Z));

        target.rawResponseListener("$3=1");
        assertEquals(true, target.isInvertDirection(Axis.X));
        assertEquals(false, target.isInvertDirection(Axis.Y));
        assertEquals(false, target.isInvertDirection(Axis.Z));

        target.rawResponseListener("$3=2");
        assertEquals(false, target.isInvertDirection(Axis.X));
        assertEquals(true, target.isInvertDirection(Axis.Y));
        assertEquals(false, target.isInvertDirection(Axis.Z));

        target.rawResponseListener("$3=4");
        assertEquals(false, target.isInvertDirection(Axis.X));
        assertEquals(false, target.isInvertDirection(Axis.Y));
        assertEquals(true, target.isInvertDirection(Axis.Z));

        target.rawResponseListener("$3=7");
        assertEquals(true, target.isInvertDirection(Axis.X));
        assertEquals(true, target.isInvertDirection(Axis.Y));
        assertEquals(true, target.isInvertDirection(Axis.Z));
    }

    @Test
    public void setInvertDirectionXShouldSetBit() throws FirmwareSettingsException, InterruptedException {
        when(controller.isStreaming()).thenReturn(false);
        when(controller.isCommOpen()).thenReturn(true);
        target.rawResponseListener("$3=7");

        // Try setting X to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.X, false);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);

        target.rawResponseListener("ok");
        assertEquals("6", target.getSetting("$3").get().getValue());


        // Try setting X to true
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.X, true);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);
        target.rawResponseListener("ok");

        assertEquals("7", target.getSetting("$3").get().getValue());
    }

    @Test
    public void setInvertDirectionYShouldSetBit() throws FirmwareSettingsException, InterruptedException {
        when(controller.isStreaming()).thenReturn(false);
        when(controller.isCommOpen()).thenReturn(true);
        target.rawResponseListener("$3=7");

        // Try setting Y to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Y, false);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);
        target.rawResponseListener("ok");
        assertEquals("5", target.getSetting("$3").get().getValue());


        // Try setting Y to true
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Y, true);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);
        target.rawResponseListener("ok");

        assertEquals("7", target.getSetting("$3").get().getValue());
    }

    @Test
    public void setInvertDirectionZShouldSetBit() throws FirmwareSettingsException, InterruptedException {
        when(controller.isStreaming()).thenReturn(false);
        when(controller.isCommOpen()).thenReturn(true);
        target.rawResponseListener("$3=7");

        // Try setting Z to false
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Z, false);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);
        target.rawResponseListener("ok");
        assertEquals("3", target.getSetting("$3").get().getValue());


        // Try setting Z to true
        ThreadHelper.invokeLater(() -> {
            try {
                target.setInvertDirection(Axis.Z, true);
            } catch (FirmwareSettingsException e) {
                fail("Should never get here but got exception: " + e);
            }
        });
        Thread.sleep(100);
        target.rawResponseListener("ok");

        assertEquals("7", target.getSetting("$3").get().getValue());
    }
}
