package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

public class GrblFirmwareSettingsInterceptorTest {
    private GrblFirmwareSettingsInterceptor target;

    private GrblFirmwareSettings firmwareSettings;

    @Before
    public void setUp() {
        IController controller = mock(IController.class);
        firmwareSettings = mock(GrblFirmwareSettings.class);
        target = new GrblFirmwareSettingsInterceptor(controller, firmwareSettings);
    }

    @Test
    public void shouldUpdateSettingsOnFirmwareSettingsCommandWithOkResponse() {
        ArgumentCaptor<FirmwareSetting> firmwareSettingArgumentCaptor = ArgumentCaptor.forClass(FirmwareSetting.class);
        doNothing().when(firmwareSettings).updateFirmwareSetting(firmwareSettingArgumentCaptor.capture());

        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$21=0");
        command.appendResponse("$22=1");
        command.appendResponse("ok");
        target.commandComplete(command);

        verify(firmwareSettings, times(2)).updateFirmwareSetting(any());
        List<FirmwareSetting> settingUpdates = firmwareSettingArgumentCaptor.getAllValues();
        assertEquals(2, settingUpdates.size());
        assertEquals("$21", settingUpdates.get(0).getKey());
        assertEquals("0", settingUpdates.get(0).getValue());
        assertEquals("$22", settingUpdates.get(1).getKey());
        assertEquals("1", settingUpdates.get(1).getValue());
    }

    @Test
    public void shouldNotUpdateSettingsOnFirmwareSettingsCommandWithErrorResponse() {
        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$21=0");
        command.appendResponse("$22=0");
        command.appendResponse("error");
        target.commandComplete(command);
        verify(firmwareSettings, times(0)).updateFirmwareSetting(any());
    }

    @Test
    public void shouldUpdateSettingOnFirmwareSettingCommandWithOkResponse() {
        ArgumentCaptor<FirmwareSetting> firmwareSettingArgumentCaptor = ArgumentCaptor.forClass(FirmwareSetting.class);
        doNothing().when(firmwareSettings).updateFirmwareSetting(firmwareSettingArgumentCaptor.capture());

        GcodeCommand command = new GcodeCommand("$21=1");
        command.appendResponse("ok");
        target.commandComplete(command);

        verify(firmwareSettings, times(1)).updateFirmwareSetting(any());
        List<FirmwareSetting> settingUpdates = firmwareSettingArgumentCaptor.getAllValues();
        assertEquals(1, settingUpdates.size());
        assertEquals("$21", settingUpdates.get(0).getKey());
        assertEquals("1", settingUpdates.get(0).getValue());
    }

    @Test
    public void shouldUpdateSettingOnFirmwareSettingCommandWithErrorResponse() {
        GcodeCommand command = new GcodeCommand("$21=1");
        command.appendResponse("error");
        target.commandComplete(command);
        verify(firmwareSettings, times(0)).updateFirmwareSetting(any());
    }
}