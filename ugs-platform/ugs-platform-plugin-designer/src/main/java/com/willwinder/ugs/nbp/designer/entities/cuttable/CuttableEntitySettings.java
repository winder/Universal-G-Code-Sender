package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;

import java.util.Optional;
import java.util.logging.Logger;

public class CuttableEntitySettings {
    private static final Logger LOGGER = Logger.getLogger(AbstractCuttable.class.getSimpleName());
    private final Cuttable cuttable;

    public CuttableEntitySettings(Cuttable cuttable) {
        this.cuttable = cuttable;
    }

    public Optional<Object> getEntitySetting(EntitySetting entitySetting) {
        return switch (entitySetting) {
            case CUT_TYPE -> Optional.of(cuttable.getCutType());
            case START_DEPTH -> Optional.of(cuttable.getStartDepth());
            case TARGET_DEPTH -> Optional.of(cuttable.getTargetDepth());
            case SPINDLE_SPEED -> Optional.of(cuttable.getSpindleSpeed());
            case PASSES -> Optional.of(cuttable.getPasses());
            case FEED_RATE -> Optional.of(cuttable.getFeedRate());
            default -> Optional.empty();
        };
    }

    public void setEntitySetting(EntitySetting entitySetting, Object value) {
        switch (entitySetting) {
            case CUT_TYPE -> cuttable.setCutType((CutType) value);
            case START_DEPTH -> cuttable.setStartDepth((Double) value);
            case TARGET_DEPTH -> cuttable.setTargetDepth((Double) value);
            case SPINDLE_SPEED -> cuttable.setSpindleSpeed((Integer) value);
            case PASSES -> cuttable.setPasses(Integer.parseInt(value.toString()));
            case FEED_RATE -> cuttable.setFeedRate(((Double) value).intValue());
            default -> LOGGER.info("Do not know how to set " + entitySetting + " to " + value);
        }
    }

}
