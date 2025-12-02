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
package com.willwinder.ugs.nbp.designer.entities.settings;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import org.openide.util.lookup.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings manager specialized for Cuttable entities.
 * Handles cutting-specific settings like cut type, depths, speeds, passes, etc.
 *
 * @author giro-dev
 */
@ServiceProvider(service = EntitySettingsManager.class, position = 2)
public class CuttableSettingsManager implements EntitySettingsManager {

    @Override
    public boolean canHandle(Entity entity) {
        return entity instanceof Cuttable;
    }

    @Override
    public List<EntitySetting> getSupportedSettings(Entity entity) {
        if (!(entity instanceof Cuttable)) {
            return List.of();
        }
        return getCuttingSettings();
    }

    @Override
    public boolean supportsSetting(EntitySetting setting) {
        return getCuttingSettings().contains(setting);
    }

    @Override
    public Object getSettingValue(EntitySetting setting, Entity entity) {
        if (!(entity instanceof Cuttable cuttable)) {
            return null;
        }

        return switch (setting) {
            case CUT_TYPE -> cuttable.getCutType();
            case START_DEPTH -> cuttable.getStartDepth();
            case TARGET_DEPTH -> cuttable.getTargetDepth();
            case SPINDLE_SPEED -> cuttable.getSpindleSpeed();
            case PASSES -> cuttable.getPasses();
            case FEED_RATE -> cuttable.getFeedRate();
            case LEAD_IN_PERCENT -> cuttable.getLeadInPercent();
            case LEAD_OUT_PERCENT -> cuttable.getLeadOutPercent();
            case INCLUDE_IN_EXPORT -> cuttable.getIncludeInExport();
            default -> null;
        };
    }

    @Override
    public void applySetting(EntitySetting setting, Object value, Entity entity) {
        if (!(entity instanceof Cuttable cuttable)) {
            return;
        }

        switch (setting) {
            case CUT_TYPE -> {
                if (value != null) {
                    cuttable.setCutType((com.willwinder.ugs.nbp.designer.entities.cuttable.CutType) value);
                }
            }
            case START_DEPTH -> {
                if (value instanceof Double depth) {
                    cuttable.setStartDepth(depth);
                }
            }
            case TARGET_DEPTH -> {
                if (value instanceof Double depth) {
                    cuttable.setTargetDepth(depth);
                }
            }
            case SPINDLE_SPEED -> {
                if (value instanceof Integer speed) {
                    cuttable.setSpindleSpeed(speed);
                }
            }
            case PASSES -> {
                if (value instanceof Integer passes) {
                    cuttable.setPasses(passes);
                }
            }
            case FEED_RATE -> {
                if (value instanceof Integer rate) {
                    cuttable.setFeedRate(rate);
                }
            }
            case LEAD_IN_PERCENT -> {
                if (value instanceof Integer percent) {
                    cuttable.setLeadInPercent(percent);
                }
            }
            case LEAD_OUT_PERCENT -> {
                if (value instanceof Integer percent) {
                    cuttable.setLeadOutPercent(percent);
                }
            }
            case INCLUDE_IN_EXPORT -> {
                if (value instanceof Boolean include) {
                    cuttable.setIncludeInExport(include);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "CuttableSettingsManager";
    }

    /**
     * Gets all cutting-related settings that apply to Cuttable entities
     */
    private static List<EntitySetting> getCuttingSettings() {
        List<EntitySetting> settings = new ArrayList<>();
        settings.add(EntitySetting.CUT_TYPE);
        settings.add(EntitySetting.START_DEPTH);
        settings.add(EntitySetting.TARGET_DEPTH);
        settings.add(EntitySetting.SPINDLE_SPEED);
        settings.add(EntitySetting.PASSES);
        settings.add(EntitySetting.FEED_RATE);
        settings.add(EntitySetting.LEAD_IN_PERCENT);
        settings.add(EntitySetting.LEAD_OUT_PERCENT);
        settings.add(EntitySetting.INCLUDE_IN_EXPORT);
        return settings;
    }
}