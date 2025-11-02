/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.actions.BaseAction;
import static com.willwinder.universalgcodesender.fx.actions.LongPressMouseEventProxy.MOUSE_LONG_PRESSED;
import static com.willwinder.universalgcodesender.fx.actions.LongPressMouseEventProxy.MOUSE_LONG_RELEASE;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;

public class JogAction extends BaseAction {
    private final JogButtonEnum jogButtonEnum;
    private static ContinuousJogWorker continuousJogWorker;
    private static JogService jogService;


    public JogAction(JogButtonEnum jogButtonEnum) {
        super(jogButtonEnum.getLabel(), jogButtonEnum.getTitle(), Localization.getString("actions.category.jogging"), jogButtonEnum.getIconUrl());
        this.jogButtonEnum = jogButtonEnum;

        // Load an instance of the continuous jog worker for all actions to share
        if (continuousJogWorker == null) {
            BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            jogService = new JogService(backend);
            continuousJogWorker = new ContinuousJogWorker(backend, jogService);
        }
    }

    @Override
    public String getId() {
        return super.getId() + jogButtonEnum.name();
    }

    @Override
    public void handleAction(ActionEvent event) {
        if (jogButtonEnum == JogButtonEnum.BUTTON_CANCEL) {
            jogService.cancelJog();
            return;
        }

        PartialPosition distance = getJogDistance();
        jogService.adjustManualLocation(distance, 1);
    }

    private PartialPosition getJogDistance() {
        PartialPosition.Builder builder = PartialPosition.builder(UnitUtils.Units.MM);

        if (jogButtonEnum.getX() != 0) {
            builder.setX(jogButtonEnum.getX() * jogService.getStepSizeXY());
        }

        if (jogButtonEnum.getY() != 0) {
            builder.setY(jogButtonEnum.getY() * jogService.getStepSizeXY());
        }

        if (jogButtonEnum.getZ() != 0) {
            builder.setZ(jogButtonEnum.getZ() * jogService.getStepSizeZ());
        }

        if (jogButtonEnum.getA() != 0) {
            builder.setA(jogButtonEnum.getA() * jogService.getStepSizeABC());
        }

        if (jogButtonEnum.getB() != 0) {
            builder.setB(jogButtonEnum.getB() * jogService.getStepSizeABC());
        }

        if (jogButtonEnum.getC() != 0) {
            builder.setC(jogButtonEnum.getC() * jogService.getStepSizeABC());
        }

        return builder.build();
    }

    @Override
    public void handleMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getEventType() == MOUSE_LONG_PRESSED) {
            continuousJogWorker.setDirection(jogButtonEnum.getX(), jogButtonEnum.getY(), jogButtonEnum.getZ(), jogButtonEnum.getA(), jogButtonEnum.getB(), jogButtonEnum.getC());
            continuousJogWorker.start();
        } else if (continuousJogWorker.isWorking() && (mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED || mouseEvent.getEventType() == MOUSE_LONG_RELEASE)) {
            continuousJogWorker.stop();
        } else if (mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {
            handleAction(new ActionEvent());
        }
    }
}
