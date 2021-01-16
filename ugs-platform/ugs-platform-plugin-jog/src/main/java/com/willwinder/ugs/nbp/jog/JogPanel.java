/*
    Copyright 2018-2021 Will Winder

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
package com.willwinder.ugs.nbp.jog;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.uielements.jog.StepSizeSpinner;
import com.willwinder.universalgcodesender.utils.FontUtils;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A panel for displaying jog controls
 *
 * @author Joacim Breiler
 */
public class JogPanel extends JPanel implements SteppedSizeManager.SteppedSizeChangeListener, ChangeListener {
    /**
     * The minimum width and height of the jog buttons.
     */
    private static final int MINIMUM_BUTTON_SIZE = 36;

    private static final float FONT_SIZE_LABEL_SMALL = 10;
    private static final float FONT_SIZE_LABEL_MEDIUM = 12;
    private static final float FONT_SIZE_LABEL_LARGE = 14;

    /**
     * How long should the jog button be pressed before continuous
     * jog is activated. Given in milliseconds
     */
    private static final int LONG_PRESS_DELAY = 500;

    /**
     * A list of listeners
     */
    private final Set<JogPanelListener> listeners = new HashSet<>();

    /**
     * A map with all buttons that allows bi-directional lookups with key->value and value->key
     */
    private final BiMap<JogPanelButtonEnum, JButton> jogButtons = HashBiMap.create();

    /**
     * Labels
     */
    private JLabel feedRateLabel;
    private JLabel xyStepLabel;
    private JLabel zStepLabel;
    private JLabel abcStepLabel;

    /**
     * Spinners for jog settings
     */
    private StepSizeSpinner feedRateSpinner;
    private StepSizeSpinner xyStepSizeSpinner;
    private StepSizeSpinner zStepSizeSpinner;
    private StepSizeSpinner abcStepSizeSpinner;

    /**
     * Special buttons
     */
    private JButton stealFocusButton;
    private JButton unitToggleButton;
    private JButton increaseStepSizeButton;
    private JButton decreaseStepSizeButton;

    public JogPanel() {
        createComponents();
        initPanels();
        initListeners();
    }

    private static boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme"); //NOI18N
    }

    private void createComponents() {

        Font font = FontUtils.getSansFont().deriveFont(Font.PLAIN, FONT_SIZE_LABEL_LARGE);

        // Create our focus stealing button first
        stealFocusButton = createImageButton("icons/keyboard.png");
        stealFocusButton.setFocusable(true);
        stealFocusButton.setToolTipText(Localization.getString("platform.plugin.jog.stealKeyboardFocus"));
        stealFocusButton.addActionListener(e -> stealFocusButton.requestFocusInWindow());

        // Create our buttons
        Arrays.asList(JogPanelButtonEnum.values()).forEach(this::createJogButton);
        Dimension minimumSize = new Dimension(80, 18);
        feedRateSpinner = new StepSizeSpinner();
        feedRateSpinner.setMinimumSize(minimumSize);
        xyStepSizeSpinner = new StepSizeSpinner();
        xyStepSizeSpinner.setMinimumSize(minimumSize);
        zStepSizeSpinner = new StepSizeSpinner();
        zStepSizeSpinner.setMinimumSize(minimumSize);
        abcStepSizeSpinner = new StepSizeSpinner();
        abcStepSizeSpinner.setMinimumSize(minimumSize);

        // todo: could use a number of factory methods here to build similar stuff
        feedRateLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.feedRate"));
        feedRateLabel.setMinimumSize(new Dimension(0,0));
        xyStepLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.stepSizeXY"));
        xyStepLabel.setMinimumSize(new Dimension(0,0));
        zStepLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.stepSizeZ"));
        zStepLabel.setMinimumSize(new Dimension(0,0));
        abcStepLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.stepSizeABC"));
        abcStepLabel.setMinimumSize(new Dimension(0,0));

        minimumSize = new Dimension(50, 18);
        unitToggleButton = new JButton("--");
        unitToggleButton.setFocusable(false);
        unitToggleButton.setMinimumSize(minimumSize);

        increaseStepSizeButton = new JButton(Localization.getString("platform.plugin.jog.stepLarger"));
        increaseStepSizeButton.setFocusable(false);
        increaseStepSizeButton.setMinimumSize(minimumSize);

        decreaseStepSizeButton = new JButton(Localization.getString("platform.plugin.jog.stepSmaller"));
        decreaseStepSizeButton.setFocusable(false);
        decreaseStepSizeButton.setMinimumSize(minimumSize);

        updateColors();
    }

    private void updateColors() {
        if (isDarkLaF()) {
            jogButtons.values().forEach(button -> setForeground(ThemeColors.LIGHT_BLUE));
            feedRateLabel.setForeground(ThemeColors.ORANGE);
            xyStepLabel.setForeground(ThemeColors.ORANGE);
            zStepLabel.setForeground(ThemeColors.ORANGE);
            unitToggleButton.setForeground(ThemeColors.ORANGE);
            increaseStepSizeButton.setForeground(ThemeColors.ORANGE);
            decreaseStepSizeButton.setForeground(ThemeColors.ORANGE);
        }
    }

    private JLabel createSettingLabel(Font font, String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalTextPosition(SwingConstants.RIGHT);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setFont(font);
        return label;
    }

    public void setFeedRate(int feedRate) {
        feedRateSpinner.setValue(String.valueOf(feedRate));
    }

    public void setStepSizeXY(double stepSize) {
        xyStepSizeSpinner.setValue(stepSize);
    }

    public void setStepSizeZ(double stepSize) {
        zStepSizeSpinner.setValue(stepSize);
    }

    public void setStepSizeABC(double stepSize) {
        abcStepSizeSpinner.setValue(stepSize);
    }

    public void setUnit(UnitUtils.Units unit) {
        unitToggleButton.setText(unit.getDescription());
    }

    public void enabledStepSizes(boolean useStepSizeZ, boolean useStepSizeABC) {
        if (useStepSizeZ || useStepSizeABC) {
            xyStepLabel.setText(Localization.getString("platform.plugin.jog.stepSizeXY"));
        } else {
            xyStepLabel.setText(Localization.getString("platform.plugin.jog.stepSize"));
        }

        zStepLabel.setVisible(useStepSizeZ);
        zStepSizeSpinner.setVisible(useStepSizeZ);
        abcStepLabel.setVisible(useStepSizeABC);
        abcStepSizeSpinner.setVisible(useStepSizeABC);
    }

    private void initPanels() {
        setLayout(new MigLayout("fill, inset 5, gap 7"));
        add(createXYZPanel(), "grow, wrap");
        add(createConfigurationPanel(), "grow");
    }

    private JPanel createConfigurationPanel() {
        JPanel configurationPanel = new JPanel();
        configurationPanel.setLayout(new MigLayout("fill, inset 0, gap 2, flowy", "[shrinkprio 200, right][25%, shrinkprio 100][25%, center, shrinkprio 0, nogrid]", "[center][center][center]"));

        configurationPanel.add(xyStepLabel, "growx");
        configurationPanel.add(zStepLabel, "growx, hidemode 3");
        configurationPanel.add(abcStepLabel, "growx, hidemode 3");
        configurationPanel.add(feedRateLabel, "growx, wrap");

        configurationPanel.add(xyStepSizeSpinner, "growx");
        configurationPanel.add(zStepSizeSpinner, "growx, hidemode 3");
        configurationPanel.add(abcStepSizeSpinner, "growx, hidemode 3");
        configurationPanel.add(feedRateSpinner, "growx, wrap");

        configurationPanel.add(unitToggleButton, "grow");
        configurationPanel.add(increaseStepSizeButton, "grow");
        configurationPanel.add(decreaseStepSizeButton, "grow");

        return configurationPanel;
    }

    private JPanel createXYZPanel() {
        JPanel xyzPanel = new JPanel();
        xyzPanel.setLayout(new MigLayout("fill, wrap 4, inset 0, gap 2", "[25%, center][25%, center][25%, center][25%, center]", "[33%, center][33%, center][33%, center]"));

        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_DIAG_XNEG_YPOS), "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_YPOS), "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_DIAG_XPOS_YPOS), "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_ZPOS), "grow");

        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_XNEG), "grow");
        xyzPanel.add(stealFocusButton, "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_XPOS), "grow");

        JPanel space = new JPanel();
        space.setOpaque(false);
        xyzPanel.add(space, "grow");

        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_DIAG_XNEG_YNEG), "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_YNEG), "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_DIAG_XPOS_YNEG), "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_ZNEG), "grow");
        return xyzPanel;
    }


    private void initListeners() {

        // Creates a window size listener
        SteppedSizeManager sizer = new SteppedSizeManager(this,
                new Dimension(300, 0), // Scaling fonts to extra small
                new Dimension(400, 0)  // Scaling fonts to small
        );
        sizer.addListener(this);


        LongPressMouseListener longPressMouseListener = new LongPressMouseListener(LONG_PRESS_DELAY) {
            @Override
            protected void onMouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return; // ignore RMB
                }
                JogPanelButtonEnum buttonEnum = getButtonEnumFromMouseEvent(e);
                listeners.forEach(a -> a.onJogButtonClicked(buttonEnum));
            }

            @Override
            protected void onMousePressed(MouseEvent e) {

            }

            @Override
            protected void onMouseRelease(MouseEvent e) {

            }

            @Override
            protected void onMouseLongPressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return; // ignore RMB
                }
                JogPanelButtonEnum buttonEnum = getButtonEnumFromMouseEvent(e);
                listeners.forEach(a -> a.onJogButtonLongPressed(buttonEnum));
            }

            @Override
            protected void onMouseLongRelease(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return; // ignore RMB
                }
                JogPanelButtonEnum buttonEnum = getButtonEnumFromMouseEvent(e);
                listeners.forEach(a -> a.onJogButtonLongReleased(buttonEnum));
            }
        };

        jogButtons.values().forEach(button -> button.addMouseListener(longPressMouseListener));

        xyStepSizeSpinner.addChangeListener(this);
        zStepSizeSpinner.addChangeListener(this);
        abcStepSizeSpinner.addChangeListener(this);
        feedRateSpinner.addChangeListener(this);

        unitToggleButton.addActionListener((actionEvent) -> listeners.forEach(JogPanelListener::onToggleUnit));
        increaseStepSizeButton.addActionListener((actionEvent) -> listeners.forEach(JogPanelListener::onIncreaseStepSize));
        decreaseStepSizeButton.addActionListener((actionEvent) -> listeners.forEach(JogPanelListener::onDecreaseStepSize));
    }

    /**
     * Finds the button enum based on the mouse event source
     *
     * @param mouseEvent the event that we want to extract the button enum from
     * @return the enum for the button
     */
    private JogPanelButtonEnum getButtonEnumFromMouseEvent(MouseEvent mouseEvent) {
        JButton releasedButton = (JButton) mouseEvent.getSource();
        return jogButtons.inverse().get(releasedButton);
    }

    /**
     * Returns the button from the button map using a button enum
     *
     * @param buttonEnum the button enum
     * @return the button
     */
    private JButton getButtonFromEnum(JogPanelButtonEnum buttonEnum) {
        return jogButtons.get(buttonEnum);
    }

    /**
     * Creates a image button with a text.
     *
     * @param buttonEnum the button enumeration containing the text  icon image
     * @return the button
     */
    private JButton createJogButton(JogPanelButtonEnum buttonEnum) {
        JButton button = new JogButton(buttonEnum);
        button.setMinimumSize(new Dimension(MINIMUM_BUTTON_SIZE, MINIMUM_BUTTON_SIZE));
        button.addActionListener((e) -> stealFocusButton.requestFocusInWindow());
        jogButtons.put(buttonEnum, button);
        return button;
    }

    /**
     * Creates a image button.
     *
     * @param baseUri the base uri of the image
     * @return the button
     */
    private JButton createImageButton(String baseUri) {
        ImageIcon imageIcon = ImageUtilities.loadImageIcon(baseUri, false);
        JButton button = new JButton(imageIcon);
        button.setMinimumSize(new Dimension(MINIMUM_BUTTON_SIZE, MINIMUM_BUTTON_SIZE));
        button.setMargin(new Insets(0,0,0,0));
        button.setFocusable(false);
        button.addActionListener((e) -> stealFocusButton.requestFocusInWindow());
        return button;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        jogButtons.values().forEach(button -> button.setEnabled(enabled));

        xyStepSizeSpinner.setEnabled(enabled);
        zStepSizeSpinner.setEnabled(enabled);
        abcStepSizeSpinner.setEnabled(enabled);
        feedRateSpinner.setEnabled(enabled);

        xyStepLabel.setEnabled(enabled);
        zStepLabel.setEnabled(enabled);
        abcStepLabel.setEnabled(enabled);
        feedRateLabel.setEnabled(enabled);

        stealFocusButton.setEnabled(enabled);

        unitToggleButton.setEnabled(enabled);
        increaseStepSizeButton.setEnabled(enabled);
        decreaseStepSizeButton.setEnabled(enabled);
    }

    @Override
    public void onSizeChange(int size) {
        switch (size) {
            case 0:
                setFontSize(FONT_SIZE_LABEL_SMALL);
                break;
            case 1:
                setFontSize(FONT_SIZE_LABEL_MEDIUM);
                break;
            default:
                setFontSize(FONT_SIZE_LABEL_LARGE);
                break;
        }
    }

    private void setFontSize(float fontSize) {
        Font font = unitToggleButton.getFont().deriveFont(fontSize);
        unitToggleButton.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(fontSize);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(fontSize);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(fontSize);
        this.zStepLabel.setFont(font);

        font = this.abcStepLabel.getFont().deriveFont(fontSize);
        this.abcStepLabel.setFont(font);

        font = this.increaseStepSizeButton.getFont().deriveFont(fontSize);
        this.increaseStepSizeButton.setFont(font);

        font = this.decreaseStepSizeButton.getFont().deriveFont(fontSize);
        this.decreaseStepSizeButton.setFont(font);
    }

    public void addListener(JogPanelListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == zStepSizeSpinner) {
            this.listeners.forEach(listener -> listener.onStepSizeZChanged(zStepSizeSpinner.getValue()));
        } else if (e.getSource() == xyStepSizeSpinner) {
            this.listeners.forEach(listener -> listener.onStepSizeXYChanged(xyStepSizeSpinner.getValue()));
        } else if (e.getSource() == abcStepSizeSpinner) {
            this.listeners.forEach(listener -> listener.onStepSizeABCChanged(abcStepSizeSpinner.getValue()));
        } else if (e.getSource() == feedRateSpinner) {
            this.listeners.forEach(listener -> listener.onFeedRateChanged(feedRateSpinner.getValue().intValue()));
        }
    }
}
