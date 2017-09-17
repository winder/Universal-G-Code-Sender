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
package com.willwinder.ugs.platform.probe;

import com.willwinder.ugs.platform.probe.AbstractProbeService.ProbeContext;
import static com.willwinder.ugs.platform.probe.ProbeService.retractDistance;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 * @author wwinder
 */
public class ProbeServiceTest {
    
    BackendAPI backend = Mockito.mock(BackendAPI.class);

    private void validateOutside(AbstractProbeService ps, ProbeContext pc) throws Exception {
        Position probeY = new Position(pc.ySpacing, 2.1, 0, Units.MM);
        Position probeX = new Position(pc.xSpacing, 1.9, 0, Units.MM);
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_SENDING));
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_IDLE));
        ps.UGSEvent(new UGSEvent(probeY));
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_SENDING));
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_IDLE));
        ps.UGSEvent(new UGSEvent(probeY));
        ps.UGSEvent(new UGSEvent(new ControllerStatus(null, probeY,null,0.,0.,null,null,null,null)));
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_SENDING));
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_IDLE));
        ps.UGSEvent(new UGSEvent(probeX));
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_SENDING));
        ps.UGSEvent(new UGSEvent(UGSEvent.ControlState.COMM_IDLE));
        ps.UGSEvent(new UGSEvent(probeX));
        ps.UGSEvent(new UGSEvent(new ControllerStatus(null, probeX,null,0.,0.,null,null,null,null)));

        // probe Y axis
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 X" + pc.xSpacing);
        verify(backend, times(1)).probe("Y", pc.feedRate, pc.ySpacing, Units.MM);
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 Y" + retractDistance(pc.ySpacing));
        verify(backend, times(1)).probe("Y", pc.feedRate/2, pc.ySpacing, Units.MM);
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 Y" + (pc.startPosition.y-probeY.y));
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 X" + -pc.xSpacing);

        // probe X axis
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 Y" + pc.ySpacing);
        verify(backend, times(1)).probe("X", pc.feedRate, pc.xSpacing, Units.MM);
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 X" + retractDistance(pc.ySpacing));
        verify(backend, times(1)).probe("X", pc.feedRate/2, pc.xSpacing, Units.MM);
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 X" + (pc.startPosition.x-probeX.x));
        verify(backend, times(1)).sendGcodeCommand(true, "G91 G21 G0 Y" + -pc.xSpacing);
        // TODO: update WCS
    }

    @Test
    @Ignore
    public void testProbeService1Outside() throws Exception {
        doReturn(true).when(backend).isIdle();

        ProbeService ps = new ProbeService(backend);

        ProbeContext pc = new ProbeService.ProbeContext(1, new Position(5, 5, 5, Units.MM), 10, 10, 100, 0);
        ps.performOutsideCornerProbe(pc);

        validateOutside(ps, pc);
    }

    @Test
    public void testProbeService2Outside() throws Exception {
        doReturn(true).when(backend).isIdle();

        ProbeService2 ps = new ProbeService2(backend);

        ProbeContext pc = new ProbeContext(1, new Position(5, 5, 5, Units.MM), 10, 10, 100, 0);
        ps.performOutsideCornerProbe(pc);

        validateOutside(ps, pc);
    }
}
