/*
    Copyright 2024 Albert Giro Quer

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
package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * @author giro-dev
 */
public class EntitySettingsManager {

    /**
     * Gets all applicable settings for the given entities
     */
    public static List<EntitySetting> getApplicableSettings(List<Entity> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }

        // For mixed selections, return the intersection of all supported settings
        List<EntitySetting> applicableSettings = getEntitySettings(entities.get(0));
        
        for (int i = 1; i < entities.size(); i++) {
            List<EntitySetting> entitySettings = getEntitySettings(entities.get(i));
            applicableSettings = applicableSettings.stream()
                    .filter(entitySettings::contains)
                    .toList();
        }
        
        return applicableSettings;
    }

    /**
     * Gets the settings supported by a specific entity
     */
    public static List<EntitySetting> getEntitySettings(Entity entity) {
        List<EntitySetting> settings = entity.getSettings();
        if (!settings.isEmpty()) {
            return settings;
        }

        // Fall back to default settings based on entity type
        if (entity instanceof Text) {
            return EntitySetting.DEFAULT_LASER_SETTINGS; // Text entities typically use laser settings
        } else if (entity instanceof Cuttable) {
            return EntitySetting.DEFAULT_ENDMILL_SETTINGS;
        }
        
        // For non-cuttable entities, only transformation settings apply
        return EntitySetting.TRANSFORMATION_SETTINGS;
    }

    /**
     * Gets the current value of a setting for the given entities.
     * If entities have different values, returns null to indicate mixed values.
     */
    public static Object getSettingValue(EntitySetting setting, List<Entity> entities) {
        if (entities.isEmpty()) {
            return null;
        }

        Object firstValue = getEntitySettingValue(setting, entities.get(0));
        
        // Check if all entities have the same value
        for (int i = 1; i < entities.size(); i++) {
            Object value = getEntitySettingValue(setting, entities.get(i));
            if (!java.util.Objects.equals(firstValue, value)) {
                return null; // Mixed values
            }
        }
        
        return firstValue;
    }

    /**
     * Gets the value of a specific setting from a single entity
     */
    private static Object getEntitySettingValue(EntitySetting setting, Entity entity) {
        return switch (setting) {
            case POSITION_X -> entity.getPosition().getX();
            case POSITION_Y -> entity.getPosition().getY();
            case WIDTH -> entity.getSize().getWidth();
            case HEIGHT -> entity.getSize().getHeight();
            case ROTATION -> entity.getRotation();
            case ANCHOR -> Anchor.BOTTOM_LEFT; // Default anchor for compatibility
            case LOCK_RATIO -> false; // Default lock ratio
            case TEXT -> (entity instanceof Text text) ? text.getText() : "";
            case FONT_FAMILY -> (entity instanceof Text text) ? text.getFontFamily() : "";
            case CUT_TYPE -> (entity instanceof Cuttable cuttable) ? cuttable.getCutType() : null;
            case START_DEPTH -> (entity instanceof Cuttable cuttable) ? cuttable.getStartDepth() : 0.0;
            case TARGET_DEPTH -> (entity instanceof Cuttable cuttable) ? cuttable.getTargetDepth() : 0.0;
            case SPINDLE_SPEED -> (entity instanceof Cuttable cuttable) ? cuttable.getSpindleSpeed() : 0;
            case PASSES -> (entity instanceof Cuttable cuttable) ? cuttable.getPasses() : 1;
            case FEED_RATE -> (entity instanceof Cuttable cuttable) ? cuttable.getFeedRate() : 0;
            case LEAD_IN_PERCENT -> (entity instanceof Cuttable cuttable) ? cuttable.getLeadInPercent() : 0;
            case LEAD_OUT_PERCENT -> (entity instanceof Cuttable cuttable) ? cuttable.getLeadOutPercent() : 0;
            case INCLUDE_IN_EXPORT -> true; // Default to true for all entities
        };
    }

    /**
     * Applies a setting value to a list of entities
     */
    public static void applySettingToEntities(EntitySetting setting, Object value, List<Entity> entities) {
        for (Entity entity : entities) {
            applySettingToEntity(setting, value, entity);
        }
    }

    /**
     * Applies a setting value to a single entity
     */
    private static void applySettingToEntity(EntitySetting setting, Object value, Entity entity) {
        switch (setting) {
            case POSITION_X -> {
                if (value instanceof Double x) {
                    entity.setPosition(new Point2D.Double(x, entity.getPosition().getY()));
                }
            }
            case POSITION_Y -> {
                if (value instanceof Double y) {
                    entity.setPosition(new Point2D.Double(entity.getPosition().getX(), y));
                }
            }
            case WIDTH -> {
                if (value instanceof Double width && entity instanceof AbstractEntity abstractEntity) {
                    abstractEntity.setWidth(width);
                }
            }
            case HEIGHT -> {
                if (value instanceof Double height && entity instanceof AbstractEntity abstractEntity) {
                    abstractEntity.setHeight(height);
                }
            }
            case ROTATION -> {
                if (value instanceof Double rotation) {
                    entity.setRotation(rotation);
                }
            }
            case TEXT -> {
                if (entity instanceof Text text && value instanceof String textValue) {
                    text.setText(textValue);
                }
            }
            case FONT_FAMILY -> {
                if (entity instanceof Text text && value instanceof String font) {
                    text.setFontFamily(font);
                }
            }
            case CUT_TYPE -> {
                if (entity instanceof Cuttable cuttable && value != null) {
                    cuttable.setCutType((com.willwinder.ugs.nbp.designer.entities.cuttable.CutType) value);
                }
            }
            case START_DEPTH -> {
                if (entity instanceof Cuttable cuttable && value instanceof Double depth) {
                    cuttable.setStartDepth(depth);
                }
            }
            case TARGET_DEPTH -> {
                if (entity instanceof Cuttable cuttable && value instanceof Double depth) {
                    cuttable.setTargetDepth(depth);
                }
            }
            case SPINDLE_SPEED -> {
                if (entity instanceof Cuttable cuttable && value instanceof Integer speed) {
                    cuttable.setSpindleSpeed(speed);
                }
            }
            case PASSES -> {
                if (entity instanceof Cuttable cuttable && value instanceof Integer passes) {
                    cuttable.setPasses(passes);
                }
            }
            case FEED_RATE -> {
                if (entity instanceof Cuttable cuttable && value instanceof Integer rate) {
                    cuttable.setFeedRate(rate);
                }
            }
            case LEAD_IN_PERCENT -> {
                if (entity instanceof Cuttable cuttable && value instanceof Integer percent) {
                    cuttable.setLeadInPercent(percent);
                }
            }
            case LEAD_OUT_PERCENT -> {
                if (entity instanceof Cuttable cuttable && value instanceof Integer percent) {
                    cuttable.setLeadOutPercent(percent);
                }
            }
            case INCLUDE_IN_EXPORT -> {
                // INCLUDE_IN_EXPORT is not available in the Cuttable interface,
                // so we'll skip this for now until we can determine the correct method
            }
            // ANCHOR and LOCK_RATIO are handled by the UI and don't directly modify the entity
        }
    }
}
