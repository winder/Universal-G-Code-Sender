/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Handles a group of entities and enables to set cut settings for all child entites.
 *
 * @author Joacim Breiler
 */
public class Group extends EntityGroup implements Cuttable {

    public Group() {
        setName("Group");
    }

    @Override
    public CutType getCutType() {
        List<CutType> cutTypes = getCuttableStream()
                .map(Cuttable::getCutType)
                .filter(cutType -> cutType != CutType.NONE)
                .distinct()
                .toList();

        if (!cutTypes.isEmpty()) {
            return cutTypes.get(0);
        } else {
            return CutType.NONE;
        }
    }

    @Override
    public void setCutType(CutType cutType) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setCutType(cutType);
            }
        });
    }

    @Override
    public double getTargetDepth() {
        return getCuttableStream()
                .mapToDouble(Cuttable::getTargetDepth)
                .max()
                .orElse(0);
    }

    @Override
    public void setTargetDepth(double cutDepth) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setTargetDepth(cutDepth);
            }
        });
    }

    @Override
    public double getStartDepth() {
        return getCuttableStream()
                .mapToDouble(Cuttable::getStartDepth)
                .max()
                .orElse(0);
    }

    @Override
    public void setStartDepth(double startDepth) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setStartDepth(startDepth);
            }
        });
    }

    @Override
    public boolean isHidden() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::isHidden)
                .orElse(false);
    }

    @Override
    public void setHidden(boolean hidden) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setHidden(hidden);
            }
        });
    }

    private Stream<Cuttable> getCuttableStream() {
        return getChildren().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast);
    }

    @Override
    public Entity copy() {
        Group copy = new Group();
        super.copyPropertiesTo(copy);
        getChildren().stream().map(Entity::copy).forEach(copy::addChild);
        copy.setHidden(isHidden());
        return copy;
    }

    public Optional<Entity> getFirstChild() {
        if (getChildren().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(getChildren().get(0));
    }

    @Override
    public List<EntitySetting> getSettings() {
        return Arrays.asList(
                EntitySetting.ANCHOR,
                EntitySetting.POSITION_X,
                EntitySetting.POSITION_Y,
                EntitySetting.WIDTH,
                EntitySetting.HEIGHT,
                EntitySetting.CUT_TYPE,
                EntitySetting.START_DEPTH,
                EntitySetting.TARGET_DEPTH
        );
    }
}
