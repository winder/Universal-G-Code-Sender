/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.drawer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.probe.ProbeZPane;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.ProbeZModel;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.scene.layout.BorderPane;

public class ProbeDrawer extends Drawer {

    private final ProbeZModel probeModel = new ProbeZModel();

    public ProbeDrawer() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(new ProbeZPane(backend));
        getChildren().add(borderPane);
    }

    @Override
    public void setActive(boolean active) {
        if (active) {
            VisualizerService.getInstance().addModel(probeModel);
        } else {
            VisualizerService.getInstance().removeModel(probeModel);
        }
    }
}