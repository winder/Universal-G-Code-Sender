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

import com.willwinder.ugs.nbm.visualizer.shared.RenderableUtils;
import com.willwinder.ugs.platform.probe.ProbeParameters;
import com.willwinder.universalgcodesender.model.Position;
import org.openide.util.lookup.ServiceProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@ServiceProvider(service = ProbePreviewManager.class)
public class ProbePreviewManager {
    private final Map<String, AbstractProbePreview> probePreviewMap = new ConcurrentHashMap<>();
    private final AtomicReference<AbstractProbePreview> activePreview = new AtomicReference<>();

    public void register(String name, AbstractProbePreview probePreview) {
        probePreviewMap.put(name, probePreview);
    }

    public void setActive(String name) {
        inactivate();
        AbstractProbePreview currentPreview = probePreviewMap.get(name);
        activePreview.set(currentPreview);
        if (currentPreview != null) {
            currentPreview.updateSettings();
            RenderableUtils.registerRenderable(currentPreview);
        }
    }

    public void inactivate() {
        AbstractProbePreview currentPreview = activePreview.get();
        if (currentPreview != null) {
            RenderableUtils.removeRenderable(currentPreview);
        }
    }

    public void updateContext(ProbeParameters probeParameters, Position workPosition, Position machinePosition) {
        AbstractProbePreview currentPreview = activePreview.get();
        if (currentPreview != null) {
            currentPreview.setContext(probeParameters, workPosition);
        }
    }

    public void updateSettings() {
        AbstractProbePreview currentPreview = activePreview.get();
        if (currentPreview != null) {
            currentPreview.updateSettings();
        }
    }

    public void activate() {
        AbstractProbePreview currentPreview = activePreview.get();
        if (currentPreview != null) {
            RenderableUtils.registerRenderable(currentPreview);
        }
    }
}
