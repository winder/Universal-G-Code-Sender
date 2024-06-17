package com.willwinder.ugs.nbp.designer.gui.selectionsettings;

import com.willwinder.ugs.nbp.designer.actions.ChangeCutSettingsAction;
import com.willwinder.ugs.nbp.designer.actions.ChangeFontAction;
import com.willwinder.ugs.nbp.designer.actions.ChangeTextAction;
import com.willwinder.ugs.nbp.designer.actions.MoveAction;
import com.willwinder.ugs.nbp.designer.actions.ResizeAction;
import com.willwinder.ugs.nbp.designer.actions.RotateAction;
import com.willwinder.ugs.nbp.designer.actions.UndoActionList;
import com.willwinder.ugs.nbp.designer.actions.UndoableAction;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class is responsible for listening to field events to create and execute undoable actions.
 * Every change to a textfield will update the entity using this dispatcher.
 *
 * @author Joacim Breiler
 */
public class FieldActionDispatcher implements FieldEventListener {
    private static final Logger LOGGER = Logger.getLogger(FieldActionDispatcher.class.getSimpleName());

    private final Controller controller;
    private final SelectionSettingsModel model;

    public FieldActionDispatcher(SelectionSettingsModel model, Controller controller) {
        this.model = model;
        this.controller = controller;
    }

    private static List<ChangeFontAction> createFontChangeActions(String font, Group selection) {
        return selection.getChildren().stream().filter(Text.class::isInstance).map(Text.class::cast).map(text -> new ChangeFontAction(text, font)).toList();
    }

    @Override
    public void onFieldUpdate(EntitySetting entitySetting, Object object) {
        if (model.get(entitySetting).equals(object)) {
            return;
        }

        SelectionManager selectionManager = controller.getSelectionManager();
        Group selection = selectionManager.getSelectionGroup();
        if (selection.getChildren().isEmpty()) {
            return;
        }

        List<UndoableAction> actionList = new ArrayList<>();
        if (entitySetting == EntitySetting.WIDTH || entitySetting == EntitySetting.HEIGHT) {
            actionList.addAll(handleSizeChange(entitySetting, (Double) object, selection));
        } else if (entitySetting == EntitySetting.POSITION_X) {
            actionList.add(createMovePositionXAction((Double) object, selection));
        } else if (entitySetting == EntitySetting.POSITION_Y) {
            actionList.add(createMovePositionYAction((Double) object, selection));
        } else if (entitySetting == EntitySetting.ROTATION) {
            actionList.add(new RotateAction(selection.getChildren(), selection.getCenter(), (Double) object - model.getRotation()));
        } else if (entitySetting == EntitySetting.FONT_FAMILY) {
            actionList.addAll(createFontChangeActions((String) object, selection));
        } else if (entitySetting == EntitySetting.TEXT) {
            List<ChangeTextAction> changeTextActions = selection.getChildren().stream().filter(Text.class::isInstance).map(Text.class::cast).map(text -> new ChangeTextAction(text, (String) object)).toList();
            actionList.addAll(changeTextActions);
        } else {
            actionList.add(createChangeSettingAction(selection, entitySetting, object));
        }

        if (actionList.isEmpty()) {
            LOGGER.warning(() -> "Missing undo/redo action " + entitySetting);
            return;
        }

        UndoActionList undoActionList = new UndoActionList(actionList);
        undoActionList.redo();
        controller.getUndoManager().addAction(undoActionList);
    }

    private List<UndoableAction> handleSizeChange(EntitySetting entitySetting, Double value, Group selection) {
        List<UndoableAction> actionList = new ArrayList<>();
        double currentWidth = model.getWidth();
        double currentHeight = model.getHeight();

        if (entitySetting == EntitySetting.WIDTH) {
            double width = value;
            double scale = model.getLockRatio() ? currentWidth / width : 1;
            double height = currentHeight / scale;
            actionList.add(new ResizeAction(selection.getChildren(), model.getAnchor(), selection.getSize(), new Size(width, height)));
        } else if (entitySetting == EntitySetting.HEIGHT) {
            double height = value;
            double scale = model.getLockRatio() ? currentHeight / height : 1;
            double width = currentWidth / scale;
            actionList.add(new ResizeAction(selection.getChildren(), model.getAnchor(), selection.getSize(), new Size(width, height)));
        }

        return actionList;
    }

    private MoveAction createMovePositionXAction(Double object, Group selection) {
        Point2D currentPosition = selection.getPosition(model.getAnchor());
        Point2D delta = new Point2D.Double(object - currentPosition.getX(), model.getPositionY() - currentPosition.getY());
        return new MoveAction(selection.getChildren(), delta);
    }

    private MoveAction createMovePositionYAction(Double object, Group selection) {
        Point2D currentPosition = selection.getPosition(model.getAnchor());
        Point2D delta = new Point2D.Double(model.getPositionX() - currentPosition.getX(), object - currentPosition.getY());
        return new MoveAction(selection.getChildren(), delta);
    }

    private ChangeCutSettingsAction createChangeSettingAction(Group selection, EntitySetting entitySetting, Object object) {
        return new ChangeCutSettingsAction(controller, selection.getChildren().stream().filter(Cuttable.class::isInstance).map(Cuttable.class::cast).toList(),
                entitySetting,
                object);
    }
}
