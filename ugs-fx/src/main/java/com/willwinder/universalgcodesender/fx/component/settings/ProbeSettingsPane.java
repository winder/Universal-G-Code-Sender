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
package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class ProbeSettingsPane extends VBox {

    public ProbeSettingsPane() {
        super(20);
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        boolean useMetric = backend.getSettings().getPreferredUnits() == UnitUtils.Units.MM;
        addTitleSection();
        ProbeSettings settings = ProbeSettings.getInstance();

        UnitTextField probeZDistance = new UnitTextField(settings.probeZDistanceProperty().getValue(), useMetric ? Unit.MM : Unit.INCH);
        UnitTextField touchPlateThickness = new UnitTextField(settings.zPlateThicknessProperty().getValue(), useMetric ? Unit.MM : Unit.INCH);
        UnitTextField probeDiameter = new UnitTextField(ProbeSettings.getProbeDiameter(), useMetric ? Unit.MM : Unit.INCH);
        UnitTextField fastFindRate = new UnitTextField(ProbeSettings.getFastFindRate(), useMetric ? Unit.MM_PER_MINUTE : Unit.INCHES_PER_MINUTE);
        UnitTextField slowFindRate = new UnitTextField(ProbeSettings.getSlowFindRate(), useMetric ? Unit.MM_PER_MINUTE : Unit.INCHES_PER_MINUTE);
        UnitTextField retractAmount = new UnitTextField(ProbeSettings.getRetractDistance(), useMetric ? Unit.MM : Unit.INCH);
        UnitTextField delayAfterRetract = new UnitTextField(ProbeSettings.getDelayAfterRetract(), Unit.SECONDS);
        SwitchButton compensateSoftLimits = new SwitchButton(ProbeSettings.getCompensateSoftLimits());

        getChildren().addAll(
                row(Localization.getString("probe.z-distance"), Localization.getString("probe.diameter.tooltip"), probeZDistance),
                row(Localization.getString("probe.plate-thickness"), Localization.getString("probe.diameter.tooltip"), touchPlateThickness),
                row(Localization.getString("probe.diameter"), Localization.getString("probe.diameter.tooltip"), probeDiameter),
                row(Localization.getString("probe.find-rate"), Localization.getString("probe.find-rate.tooltip"), fastFindRate),
                row(Localization.getString("probe.measure-rate"), Localization.getString("probe.measure-rate.tooltip"), slowFindRate),
                row(Localization.getString("probe.retract-amount"), Localization.getString("probe.retract-amount.tooltip"), retractAmount),
                row(Localization.getString("probe.delay-after-retract"), Localization.getString("probe.delay-after-retract.tooltip"), delayAfterRetract),
                row(Localization.getString("probe.compensate-for-soft-limits"), Localization.getString("probe.compensate-for-soft-limits.tooltip"), compensateSoftLimits)
        );

        probeZDistance.unitValueProperty().addListener((o, a, v) -> settings.probeZDistanceProperty().setValue(v));
        touchPlateThickness.unitValueProperty().addListener((o, a, v) -> settings.zPlateThicknessProperty().setValue(v));
        probeDiameter.unitValueProperty().addListener((o, a, v) -> ProbeSettings.setProbeDiameter(v));
        fastFindRate.unitValueProperty().addListener((o, a, v) -> ProbeSettings.setFastFindRate(v));
        slowFindRate.unitValueProperty().addListener((o, a, v) -> ProbeSettings.setSlowFindRate(v));
        retractAmount.unitValueProperty().addListener((o, a, v) -> ProbeSettings.setRetractDistance(v));
        delayAfterRetract.unitValueProperty().addListener((o, a, v) -> ProbeSettings.setDelayAfterRetract(v));
        compensateSoftLimits.selectedProperty().addListener((o, a, v) -> ProbeSettings.setCompensateSoftLimits(v));
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.probe"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

    private static HBox row(String label, String tooltip, Control control) {
        Label l = new Label(label);
        l.setWrapText(true);
        l.setPrefWidth(160);
        l.setMaxWidth(200);

        control.setTooltip(new Tooltip(tooltip));
        control.setMinWidth(80);
        return new HBox(10, l, control);
    }
}