/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.probe.renderable;

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_PROBE_PREVIEW;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.platform.probe.ProbeParameters;
import com.willwinder.ugs.platform.probe.ProbeService;
import com.willwinder.universalgcodesender.model.Position;
import org.openide.util.Lookup;

public abstract class AbstractProbePreview extends Renderable {

    private final ProbeService probeService;

    protected AbstractProbePreview(int priority, String title) {
        super(priority, title, VISUALIZER_OPTION_PROBE_PREVIEW);
        probeService = Lookup.getDefault().lookup(ProbeService.class);
    }

    public abstract void setContext(ProbeParameters pc, Position startWork);

    public abstract void updateSettings();

    public boolean isProbeCycleActive() {
        return probeService.probeCycleActive();
    }
}
