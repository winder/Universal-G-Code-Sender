/*
    Copyright 2021-2026 Joacim Breiler

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
package com.willwinder.ugs.designer.entities.cuttable;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.EntitySetting;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Handles a group of entities and enables to set cut settings for all child entities.
 *
 * @author Joacim Breiler
 */
public class Group extends EntityGroup implements Cuttable {
    private final CuttableEntitySettings entitySettings;

    public Group() {
        setName("Group");
        entitySettings = new CuttableEntitySettings(this);
    }

    public Group(List<Entity> children) {
        this();
        addAll(children);
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
    public void setToolPathAngle(double toolPathDirection) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setToolPathAngle(toolPathDirection);
            }
        });
    }

    @Override
    public double getToolPathAngle() {
        return getCuttableStream()
                .mapToDouble(Cuttable::getToolPathAngle)
                .max()
                .orElse(0);
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
    public Direction getDirection() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::getDirection)
                .orElse(Direction.CLIMB);
    }

    @Override
    public void setDirection(Direction direction) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setDirection(direction);
            }
        });
    }

    @Override
    public PlungeType getPlungeType() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::getPlungeType)
                .orElse(PlungeType.LINEAR_RAMP);
    }

    @Override
    public void setPlungeType(PlungeType plungeType) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setPlungeType(plungeType);
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
    public EnumSet<EntitySetting> getSettings() {
        List<Cuttable> cuttables = getCuttableStream().toList();
        if (cuttables.isEmpty()) {
            return EnumSet.noneOf(EntitySetting.class);
        }

        EnumSet<EntitySetting> sharedSettings = cuttables.get(0).getSettings();
        if (sharedSettings.isEmpty()) {
            return EnumSet.noneOf(EntitySetting.class);
        }

        removeSettingsMissingInAny(cuttables, sharedSettings);
        removeSettingsWithDifferentValues(cuttables, sharedSettings);
        return EnumSet.copyOf(sharedSettings.stream()
                .filter(sharedSettings::contains)
                .toList());
    }

    private static void removeSettingsMissingInAny(List<Cuttable> cuttables, Set<EntitySetting> sharedSettings) {
        for (int i = 1; i < cuttables.size() && !sharedSettings.isEmpty(); i++) {
            EnumSet<EntitySetting> settings = cuttables.get(i).getSettings();
            if (settings.isEmpty()) {
                sharedSettings.clear();
                return;
            }

            sharedSettings.retainAll(settings);
        }
    }

    /**
     * Removes any setting where the entities have differing values. Settings that don't expose a
     * value are considered shared.
     */
    private static void removeSettingsWithDifferentValues(List<Cuttable> cuttables, Set<EntitySetting> sharedSettings) {
        Map<EntitySetting, Object> valuesToCompare = getComparableValues(cuttables.get(0), sharedSettings);

        for (int i = 1; i < cuttables.size() && !valuesToCompare.isEmpty(); i++) {
            Cuttable cuttable = cuttables.get(i);
            Iterator<Map.Entry<EntitySetting, Object>> iterator = valuesToCompare.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EntitySetting, Object> valueToCompare = iterator.next();
                if (!hasSameValue(cuttable, valueToCompare)) {
                    sharedSettings.remove(valueToCompare.getKey());
                    iterator.remove();
                }
            }
        }
    }

    private static Map<EntitySetting, Object> getComparableValues(Cuttable cuttable, Set<EntitySetting> settings) {
        Map<EntitySetting, Object> values = new EnumMap<>(EntitySetting.class);
        settings.forEach(setting -> cuttable.getEntitySetting(setting)
                .ifPresent(value -> values.put(setting, value)));
        return values;
    }

    private static boolean hasSameValue(Cuttable cuttable, Map.Entry<EntitySetting, Object> valueToCompare) {
        Object value = cuttable.getEntitySetting(valueToCompare.getKey()).orElse(null);
        return Objects.deepEquals(valueToCompare.getValue(), value);
    }

    @Override
    public List<CutType> getAvailableCutTypes() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::getAvailableCutTypes)
                .orElse(Collections.emptyList());
    }

    @Override
    public void setToolPathDirection(ToolPathDirection toolPathAngle) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setToolPathDirection(toolPathAngle);
            }
        });
    }

    @Override
    public ToolPathDirection getToolPathDirection() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::getToolPathDirection)
                .orElse(ToolPathDirection.HORIZONTAL);
    }

    @Override
    public void setFinishingPass(boolean finishingPass) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setFinishingPass(finishingPass);
            }
        });
    }

    @Override
    public boolean isFinishingPass() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::isFinishingPass)
                .orElse(false);
    }

    @Override
    public void setStockToLeave(double stockToLeave) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setStockToLeave(stockToLeave);
            }
        });
    }

    @Override
    public double getStockToLeave() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::getStockToLeave)
                .orElse(DEFAULT_STOCK_TO_LEAVE);
    }

    @Override
    public void setLineSpacing(double lineSpacing) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setLineSpacing(lineSpacing);
            }
        });
    }

    @Override
    public double getLineSpacing() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::getLineSpacing)
                .orElse(DEFAULT_LINE_SPACING);
    }

    @Override
    public void setTabs(boolean tabs) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setTabs(tabs);
            }
        });
    }

    @Override
    public boolean hasTabs() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::hasTabs)
                .orElse(false);
    }

    @Override
    public void setTabCount(int tabCount) {
        getChildren().forEach(child -> {
            if (child instanceof Cuttable cuttable) {
                cuttable.setTabCount(tabCount);
            }
        });
    }

    @Override
    public int getTabCount() {
        return getCuttableStream()
                .findFirst()
                .map(Cuttable::getTabCount)
                .orElse(DEFAULT_TAB_COUNT);
    }
}
