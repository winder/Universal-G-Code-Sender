/*
    Copyright 2017 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.firmware.grbl.GrblFirmwareSettings;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author wwinder
 */
public class GrblControllerMockTests {
  @Test
  public void testFillInSettings() {
    GrblController gc = Mockito.spy(new GrblController());

    GrblFirmwareSettings firmwareSettings = Mockito.mock(GrblFirmwareSettings.class);
    Mockito.when(firmwareSettings.getReportingUnits()).thenReturn(Units.MM);
    doReturn(firmwareSettings).when(gc).getFirmwareSettings();

    String description = "Status report options, mask";
    gc.rawResponseListener("$10=100");

    ArgumentCaptor<String> consoleMessageCaptor = ArgumentCaptor.forClass(String.class);
    verify(gc, times(1)).dispatchConsoleMessage(any(), consoleMessageCaptor.capture());
    assertThat(consoleMessageCaptor.getValue(), containsString(description));
  }

  @Test
  public void testProbeResponse() {
    GrblController gc = Mockito.spy(new GrblController());

    GrblFirmwareSettings firmwareSettings = Mockito.mock(GrblFirmwareSettings.class);
    Mockito.when(firmwareSettings.getReportingUnits()).thenReturn(Units.MM);
    doReturn(firmwareSettings).when(gc).getFirmwareSettings();

    gc.rawResponseListener("[PRB:-192.200,-202.000,-40.400:1]");

    ArgumentCaptor<Position> probeCaptor = ArgumentCaptor.forClass(Position.class);
    verify(gc, times(1)).dispatchProbeCoordinates(probeCaptor.capture());
    assertThat(probeCaptor.getValue(), is(new Position(-192.200, -202.000, -40.400, Units.MM)));
  }
}
