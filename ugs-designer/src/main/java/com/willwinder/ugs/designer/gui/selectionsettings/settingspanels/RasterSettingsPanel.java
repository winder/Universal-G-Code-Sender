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
package com.willwinder.ugs.designer.gui.selectionsettings.settingspanels;

import com.willwinder.ugs.designer.actions.ChangeEntitySettingsAction;
import com.willwinder.ugs.designer.actions.UndoableAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntitySetting;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_DEPTH_CONTRAST;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_DEPTH_DETAIL;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_DEPTH_EMPHASIS;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_DEPTH_MAPPING;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_DEPTH_SMOOTHING;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_INVERT;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_LEVELS;
import static com.willwinder.ugs.designer.entities.EntitySetting.RASTER_POWER_CURVE;
import com.willwinder.ugs.designer.entities.cuttable.Group;
import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.entities.settings.RasterSettingsManager;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.utils.DepthMapGenerator;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.services.LookupServiceProvider;
import com.willwinder.universalgcodesender.uielements.components.SeparatorLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@LookupServiceProvider(position = 1)
public class RasterSettingsPanel extends JPanel implements EntitySettingsPanel {
    private static final Logger LOGGER = Logger.getLogger(RasterSettingsPanel.class.getSimpleName());
    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 36";
    private static final String MODEL_DOWNLOAD_URL = "https://raw.githubusercontent.com/winder/Universal-G-Code-Sender/refs/heads/master/models/depth-anything-v2-small/depth-anything-v2-small.onnx";
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final RasterSettingsManager settingsManager = new RasterSettingsManager();
    private boolean updating;
    private JSlider levelSlider;
    private JCheckBox invert;
    private JCheckBox depthMapping;
    private JLabel modelWarning;
    private JButton downloadModelButton;
    private JSlider detailSlider;
    private JSlider smoothingSlider;
    private JSlider contrastSlider;
    private JSlider emphasisSlider;
    private final List<JComponent> depthControls = new ArrayList<>();
    private PowerCurvePanel powerCurvePanel;


    public RasterSettingsPanel() {
        super(new MigLayout("insets 0, gap 10, fillx, wrap 2, hidemode 3", "[sg label,right] 10 [grow]"));
        initializeComponents();
        buildLayout();
        setupListeners();
    }

    private void setupListeners() {
        levelSlider.addChangeListener(e -> firePropertyChange(RASTER_LEVELS, levelSlider.getValue()));
        invert.addActionListener(e -> firePropertyChange(RASTER_INVERT, invert.isSelected()));
        depthMapping.addActionListener(e -> {
            firePropertyChange(RASTER_DEPTH_MAPPING, depthMapping.isSelected());
            setDepthControlsVisible(depthMapping.isSelected());
            refreshModelWarning();
        });
        detailSlider.addChangeListener(e -> firePropertyChange(RASTER_DEPTH_DETAIL, detailSlider.getValue() / 100.0));
        smoothingSlider.addChangeListener(e -> firePropertyChange(RASTER_DEPTH_SMOOTHING, smoothingSlider.getValue() / 100.0));
        contrastSlider.addChangeListener(e -> firePropertyChange(RASTER_DEPTH_CONTRAST, contrastSlider.getValue() / 100.0));
        emphasisSlider.addChangeListener(e -> firePropertyChange(RASTER_DEPTH_EMPHASIS, emphasisSlider.getValue() / 100.0));
        powerCurvePanel.addPropertyChangeListener(PowerCurvePanel.PROPERTY_CURVE_CHANGED,
                e -> firePropertyChange(RASTER_POWER_CURVE, e.getNewValue()));
    }

    private void firePropertyChange(EntitySetting entitySetting, Object newValue) {
        if (!updating) {
            SwingUtilities.invokeLater(() -> pcs.firePropertyChange(entitySetting.name(), null, newValue));
        }
    }

    private void buildLayout() {
        add(new SeparatorLabel(Localization.getString("designer.panel.shape-settings.raster.title"), SwingConstants.RIGHT), "spanx, growx");

        add(new JLabel(Localization.getString("platform.plugin.designer.raster.power-curve"), SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(powerCurvePanel, "grow, w 60:200:300, h 200:200:300");

        add(new JLabel(Localization.getString("platform.plugin.designer.raster.levels"), SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(levelSlider, FIELD_CONSTRAINTS);

        add(new JLabel(Localization.getString("platform.plugin.designer.raster.invert"), SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(invert, FIELD_CONSTRAINTS);

        add(new JLabel("Depth mapping", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(depthMapping, FIELD_CONSTRAINTS);

        add(modelWarning, "skip 1, alignx left, wrap");
        add(downloadModelButton, "skip 1, alignx left, wrap");

        addDepthControl("Detail", detailSlider);
        addDepthControl("Smoothing", smoothingSlider);
        addDepthControl("Contrast", contrastSlider);
        addDepthControl("Subject emphasis", emphasisSlider);
    }

    private void addDepthControl(String labelText, JSlider slider) {
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        add(label, LABEL_CONSTRAINTS);
        add(slider, FIELD_CONSTRAINTS);
        depthControls.add(label);
        depthControls.add(slider);
    }

    private void refreshModelWarning() {
        DepthMapGenerator generator = new DepthMapGenerator();
        boolean show = !generator.isModelAvailable() && depthMapping.isSelected();
        if (show) {
            modelWarning.setText("Depth model not available");
            modelWarning.setToolTipText("No depth model was found at " + generator.getModelPath()
                    + ". Depth mapping will fall back to the original image.");
            downloadModelButton.setEnabled(true);
        }
        modelWarning.setVisible(show);
        downloadModelButton.setVisible(show);
    }

    private void downloadModel() {
        Path target = new DepthMapGenerator().getModelPath();
        downloadModelButton.setEnabled(false);
        modelWarning.setText("Downloading depth model…");
        modelWarning.setToolTipText(target.toString());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Files.createDirectories(target.getParent());
                Path temp = Files.createTempFile(target.getParent(), "depth-model", ".onnx.part");
                try (InputStream in = URI.create(MODEL_DOWNLOAD_URL).toURL().openStream()) {
                    Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                }
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to download depth model from " + MODEL_DOWNLOAD_URL, e);
                    modelWarning.setText("Download failed");
                    modelWarning.setToolTipText(e.getMessage());
                    downloadModelButton.setEnabled(true);
                    return;
                }
                refreshModelWarning();
            }
        }.execute();
    }

    private static Icon scaleIcon(Icon icon, int size) {
        if (icon == null) {
            return null;
        }
        BufferedImage image = new BufferedImage(Math.max(1, icon.getIconWidth()),
                Math.max(1, icon.getIconHeight()), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        icon.paintIcon(null, graphics, 0, 0);
        graphics.dispose();
        Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private void setDepthControlsVisible(boolean visible) {
        depthControls.forEach(component -> component.setVisible(visible));
        revalidate();
        repaint();
    }

    private void initializeComponents() {
        levelSlider = createIntegerSlider(2, 255, 255);
        invert = new JCheckBox();
        depthMapping = new JCheckBox();
        modelWarning = new JLabel(scaleIcon(UIManager.getIcon("OptionPane.warningIcon"), 16));
        modelWarning.setVisible(false);
        downloadModelButton = new JButton("Download model");
        downloadModelButton.setVisible(false);
        downloadModelButton.addActionListener(e -> downloadModel());
        detailSlider = createIntegerSlider(0, 100, 60);
        smoothingSlider = createIntegerSlider(0, 100, 30);
        contrastSlider = createIntegerSlider(0, 100, 10);
        emphasisSlider = createIntegerSlider(0, 100, 33);
        powerCurvePanel = new PowerCurvePanel();
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
                levelSlider.setValue(raster.getLevels());
                invert.setSelected(raster.isInvert());
                depthMapping.setSelected(raster.isDepthMapping());
                detailSlider.setValue((int) Math.round(raster.getDepthDetail() * 100));
                smoothingSlider.setValue((int) Math.round(raster.getDepthSmoothing() * 100));
                contrastSlider.setValue((int) Math.round(raster.getDepthContrast() * 100));
                emphasisSlider.setValue((int) Math.round(raster.getDepthEmphasis() * 100));
                setDepthControlsVisible(raster.isDepthMapping());
                refreshModelWarning();
                powerCurvePanel.setControlPoints(raster.getPowerCurveControlPoints());
            } finally {
                updating = false;
            }
        }
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
                .forEach(raster -> raster.setEntitySetting(entitySetting, newValue));
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
