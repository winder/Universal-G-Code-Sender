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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Handles a group of entities and enables to set cut settings for all child entites.
 *
 * @author Joacim Breiler
 */
public class Group extends EntityGroup implements Cuttable {
    private final CuttableEntitySettings entitySettings;


    public Group() {
        setName("Group");
        entitySettings = new CuttableEntitySettings(this);
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
    public int getSpindleSpeed() {
        return getCuttableStream()
                .mapToInt(Cuttable::getSpindleSpeed)
                .max()
                .orElse(0);
    }

    @Override
    public void setSpindleSpeed(int spindleSpeed) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setSpindleSpeed(spindleSpeed);
            }
        });
    }

    @Override
    public int getFeedRate() {
        return getCuttableStream()
                .mapToInt(Cuttable::getFeedRate)
                .max()
                .orElse(0);
    }

    @Override
    public void setFeedRate(int feedRate) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setFeedRate(feedRate);
            }
        });
    }

    @Override
    public int getPasses() {
        return getCuttableStream()
                .mapToInt(Cuttable::getPasses)
                .max()
                .orElse(0);
    }

    @Override
    public void setPasses(int passes) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setPasses(passes);
            }
        });
    }

    @Override
    public int getLeadInPercent() {
        return getCuttableStream()
                .mapToInt(Cuttable::getLeadInPercent)
                .max()
                .orElse(0);
    }

    @Override
    public void setLeadInPercent(int value) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setLeadInPercent(value);
            }
        });
    }


    @Override
    public int getLeadOutPercent() {
        return getCuttableStream()
                .mapToInt(Cuttable::getLeadOutPercent)
                .max()
                .orElse(0);
    }

    @Override
    public void setLeadOutPercent(int value) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setLeadOutPercent(value);
            }
        });
    }
    @Override
    public boolean getIncludeInExport() {
        for (Entity child : getChildren()) {
            if (child instanceof Cuttable cuttable) {
                if (cuttable.getIncludeInExport()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setIncludeInExport(boolean value) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setIncludeInExport(value);
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

    @Override
    public Optional<Object> getEntitySetting(EntitySetting entitySetting) {
        return entitySettings.getEntitySetting(entitySetting);
    }

    @Override
    public void setEntitySetting(EntitySetting entitySetting, Object value) {
        entitySettings.setEntitySetting(entitySetting, value);
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

    @Override
    public List<EntitySetting> getSettings() {
        List<List<EntitySetting>> list = getCuttableStream().map(Entity::getSettings).toList();
        if (list.isEmpty()) {
            return List.of();
        }

        List<EntitySetting> result = list.get(0);
        for (List<EntitySetting> settings : list) {
            result.retainAll(settings);
        }

        // Remove cut type if they are of differnt types
        if (getCuttableStream().map(Cuttable::getCutType).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.CUT_TYPE);
        }

        if (getCuttableStream().map(Cuttable::getStartDepth).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.START_DEPTH);
        }

        if (getCuttableStream().map(Cuttable::getTargetDepth).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.TARGET_DEPTH);
        }

        if (getCuttableStream().map(Cuttable::getSpindleSpeed).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.SPINDLE_SPEED);
        }

        if (getCuttableStream().map(Cuttable::getFeedRate).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.FEED_RATE);
        }

        if (getCuttableStream().map(Cuttable::getLeadInPercent).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.LEAD_IN_PERCENT);
        }

        if (getCuttableStream().map(Cuttable::getLeadOutPercent).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.LEAD_OUT_PERCENT);
        }
        
        if (getCuttableStream().map(Cuttable::getIncludeInExport).distinct().toList().size() > 1) {
            result = new ArrayList<>(result);
            result.remove(EntitySetting.INCLUDE_IN_EXPORT);
        }
        
        return result;
    }
}
