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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.settingspanels;

import com.willwinder.ugs.nbp.designer.actions.ChangeEntitySettingsAction;
import com.willwinder.ugs.nbp.designer.actions.UndoableAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.RASTER_BRIGHTNESS;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.RASTER_CONTRAST;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.RASTER_GAMMA;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.RASTER_INVERT;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.RASTER_LEVELS;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Raster;
import com.willwinder.ugs.nbp.designer.entities.settings.RasterSettingsManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.components.DoubleSlider;
import com.willwinder.universalgcodesender.uielements.components.SeparatorLabel;
import net.miginfocom.swing.MigLayout;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

@ServiceProvider(service = EntitySettingsPanel.class, position = 1)
public class RasterSettingsPanel extends JPanel implements EntitySettingsPanel {
    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 36";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final RasterSettingsManager settingsManager = new RasterSettingsManager();
    private boolean updating;
    private DoubleSlider brightnessSlider;
    private DoubleSlider contrastSlider;
    private DoubleSlider gammaSlider;
    private JSlider levelSlider;
    private JCheckBox invert;


    public RasterSettingsPanel() {
        super(new MigLayout("insets 0, gap 10, fillx, wrap 2", "[sg label,right] 10 [grow]"));
        initializeComponents();
        buildLayout();
        setupListeners();
    }

    private void setupListeners() {
        brightnessSlider.addChangeListener(e -> firePropertyChange(RASTER_BRIGHTNESS, brightnessSlider.getDoubleValue()));
        contrastSlider.addChangeListener(e -> firePropertyChange(RASTER_CONTRAST, contrastSlider.getDoubleValue()));
        gammaSlider.addChangeListener(e -> firePropertyChange(RASTER_GAMMA, gammaSlider.getDoubleValue()));
        levelSlider.addChangeListener(e -> firePropertyChange(RASTER_LEVELS, levelSlider.getValue()));
        invert.addActionListener(e -> firePropertyChange(RASTER_INVERT, invert.isSelected()));
    }

    private void firePropertyChange(EntitySetting entitySetting, Object newValue) {
        if (!updating) {
            SwingUtilities.invokeLater(() -> pcs.firePropertyChange(entitySetting.name(), null, newValue));
        }
    }

    private void buildLayout() {
        add(new SeparatorLabel(Localization.getString("designer.panel.shape-settings.raster.title"), SwingConstants.RIGHT), "spanx, growx");

        add(new JLabel("Brightness", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(brightnessSlider, FIELD_CONSTRAINTS);

        add(new JLabel("Contrast", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(contrastSlider, FIELD_CONSTRAINTS);

        add(new JLabel("Gamma", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(gammaSlider, FIELD_CONSTRAINTS);

        add(new JLabel("Levels", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(levelSlider, FIELD_CONSTRAINTS);

        add(new JLabel("Invert", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(invert, FIELD_CONSTRAINTS);
    }

    private void initializeComponents() {
        brightnessSlider = createDoubleSlider(-1d, 1d, 100);
        contrastSlider = createDoubleSlider(0d, 3d, 100);
        gammaSlider = createDoubleSlider(0.1d, 10d, 100);
        levelSlider = createIntegerSlider(2, 255, 255);
        invert = new JCheckBox();
    }

    @Override
    public boolean isApplicable(Group selectionGroup) {
        return selectionGroup.getChildren().stream()
                .allMatch(Raster.class::isInstance);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setFromSelection(Group selectionGroup) {
        Raster raster = selectionGroup.getChildren().stream()
                .filter(Raster.class::isInstance)
                .map(Raster.class::cast)
                .findFirst()
                .orElse(null);

        if (raster != null) {
            updating = true;
            try {
                brightnessSlider.setDoubleValue(raster.getBrightness());
                contrastSlider.setDoubleValue(raster.getContrast());
                gammaSlider.setDoubleValue(raster.getGamma());
                levelSlider.setValue(raster.getLevels());
            } finally {
                updating = false;
            }
        }
    }

    private DoubleSlider createDoubleSlider(double min, double max, int steps) {
        DoubleSlider slider = new DoubleSlider(min, max, steps);
        slider.setPaintLabels(true);
        slider.setPaintTicks(false);
        slider.setSnapToTicks(true);
        return slider;
    }


    private JSlider createIntegerSlider(int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setPaintLabels(true);
        slider.setPaintTicks(false);
        slider.setSnapToTicks(true);
        return slider;
    }

    @Override
    public void applyChangeToSelection(EntitySetting entitySetting, Object newValue, Group selectionGroup) {
        selectionGroup.getChildren().stream()
                .filter(Raster.class::isInstance)
                .map(Raster.class::cast)
                .forEach(raster -> applyRasterProperty(raster, entitySetting, newValue));
    }

    private void applyRasterProperty(Raster raster, EntitySetting setting, Object newValue) {
        if (RASTER_BRIGHTNESS.equals(setting)) {
            raster.setBrightness((Double) newValue);
        } else if (RASTER_CONTRAST.equals(setting)) {
            raster.setContrast((Double) newValue);
        } else if (RASTER_GAMMA.equals(setting)) {
            raster.setGamma((Double) newValue);
        } else if (RASTER_INVERT.equals(setting)) {
            raster.setInvert((Boolean) newValue);
        } else if (RASTER_LEVELS.equals(setting)) {
            System.out.println(newValue);
            raster.setLevels((Integer) newValue);
        }
    }

    @Override
    public void addChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removeChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }


    @Override
    public void createAndExecuteUndoableAction(EntitySetting entitySetting, Object newValue, Group selectionGroup, Controller controller) {
        List<Entity> entities = selectionGroup.getChildren();
        if (entities.isEmpty()) return;

        UndoableAction action = new ChangeEntitySettingsAction(entities, entitySetting, newValue, settingsManager);
        action.redo();
        controller.getUndoManager().addAction(action);
    }
}
