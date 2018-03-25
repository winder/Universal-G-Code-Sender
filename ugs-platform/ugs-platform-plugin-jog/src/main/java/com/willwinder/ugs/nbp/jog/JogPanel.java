/*
    Copyright 2018 Will Winder

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
import com.willwinder.universalgcodesender.uielements.helpers.MachineStatusFontManager;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.uielements.jog.StepSizeSpinner;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.InputStream;
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
    private static final int MINIMUM_BUTTON_SIZE = 52;

    private static final float FONT_SIZE_LABEL_SMALL = 8;
    private static final float FONT_SIZE_LABEL_MEDIUM = 10;
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
    private final BiMap<JogPanelButtonEnum, JButton> buttons = HashBiMap.create();

    /**
     * Labels
     */
    private JLabel feedRateLabel;
    private JLabel xyStepLabel;
    private JLabel zStepLabel;

    /**
     * Spinners for jog settings
     */
    private StepSizeSpinner zStepSizeSpinner;
    private StepSizeSpinner feedRateSpinner;
    private StepSizeSpinner xyStepSizeSpinner;

    public JogPanel() {
        createComponents();
        initPanels();
        initListeners();
    }

    private static boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme"); //NOI18N
    }

    private void createComponents() {
        String fontPath = "/resources/";
        // https://www.fontsquirrel.com
        String fontName = "OpenSans-Regular.ttf";
        InputStream is = getClass().getResourceAsStream(fontPath + fontName);
        Font font = MachineStatusFontManager.createFont(is, fontName).deriveFont(Font.PLAIN, FONT_SIZE_LABEL_LARGE);

        // Create our buttons
        buttons.put(JogPanelButtonEnum.BUTTON_XPOS, createImageButton("icons/xpos.png", "X+", SwingConstants.CENTER, SwingConstants.LEFT));
        buttons.put(JogPanelButtonEnum.BUTTON_XNEG, createImageButton("icons/xneg.png", "X-", SwingConstants.CENTER, SwingConstants.RIGHT));
        buttons.put(JogPanelButtonEnum.BUTTON_YPOS, createImageButton("icons/ypos.png", "Y+", SwingConstants.BOTTOM, SwingConstants.CENTER));
        buttons.put(JogPanelButtonEnum.BUTTON_YNEG, createImageButton("icons/yneg.png", "Y-", SwingConstants.TOP, SwingConstants.CENTER));
        buttons.put(JogPanelButtonEnum.BUTTON_ZPOS, createImageButton("icons/ypos.png", "Z+", SwingConstants.BOTTOM, SwingConstants.CENTER));
        buttons.put(JogPanelButtonEnum.BUTTON_ZNEG, createImageButton("icons/yneg.png", "Z-", SwingConstants.TOP, SwingConstants.CENTER));
        buttons.put(JogPanelButtonEnum.BUTTON_DIAG_XPOS_YPOS, createImageButton("icons/diag-xpos-ypos.png"));
        buttons.put(JogPanelButtonEnum.BUTTON_DIAG_XNEG_YPOS, createImageButton("icons/diag-xneg-ypos.png"));
        buttons.put(JogPanelButtonEnum.BUTTON_DIAG_XPOS_YNEG, createImageButton("icons/diag-xpos-yneg.png"));
        buttons.put(JogPanelButtonEnum.BUTTON_DIAG_XNEG_YNEG, createImageButton("icons/diag-xneg-yneg.png"));

        feedRateSpinner = new StepSizeSpinner();
        xyStepSizeSpinner = new StepSizeSpinner();
        zStepSizeSpinner = new StepSizeSpinner();

        // todo: could use a number of factory methods here to build similar stuff
        feedRateLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.feedRate").toUpperCase());
        xyStepLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.stepSizeXY").toUpperCase());
        zStepLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.stepSizeZ").toUpperCase());

        JButton unitToggleButton = new JButton("--");
        unitToggleButton.setMinimumSize(new Dimension(MINIMUM_BUTTON_SIZE, MINIMUM_BUTTON_SIZE));
        unitToggleButton.setFocusable(false);
        buttons.put(JogPanelButtonEnum.BUTTON_TOGGLE_UNIT, unitToggleButton);

        if (isDarkLaF()) {
            buttons.values().forEach(button -> setForeground(ThemeColors.LIGHT_BLUE));
            feedRateLabel.setForeground(ThemeColors.ORANGE);
            xyStepLabel.setForeground(ThemeColors.ORANGE);
            zStepLabel.setForeground(ThemeColors.ORANGE);
            unitToggleButton.setForeground(ThemeColors.LIGHT_BLUE);
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

    public void setUnit(UnitUtils.Units unit) {
        getButtonFromEnum(JogPanelButtonEnum.BUTTON_TOGGLE_UNIT).setText(unit.name());
    }

    public void setUseStepSizeZ(boolean useStepSizeZ) {
        if (useStepSizeZ) {
            zStepLabel.setVisible(true);
            zStepSizeSpinner.setVisible(true);
            xyStepLabel.setText(Localization.getString("platform.plugin.jog.stepSizeXY").toUpperCase());
        } else {
            zStepLabel.setVisible(false);
            zStepSizeSpinner.setVisible(false);
            xyStepLabel.setText(Localization.getString("platform.plugin.jog.stepSize").toUpperCase());
        }
    }

    private void initPanels() {
        setLayout(new MigLayout("fill, inset 5, gap 7"));
        add(createXYZPanel(), "grow, wrap");
        add(createConfigurationPanel(), "grow");
    }

    private JPanel createConfigurationPanel() {
        JPanel configurationPanel = new JPanel();
        configurationPanel.setLayout(new MigLayout("fill, inset 0, gap 2", "[55%, shrinkprio 100, right][20%, shrinkprio 100][25%, center, shrinkprio 0]", "[center][center][center]"));

        configurationPanel.add(xyStepLabel, "growx");
        configurationPanel.add(xyStepSizeSpinner, "growx");
        configurationPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_TOGGLE_UNIT), "grow, spany, wrap");

        configurationPanel.add(zStepLabel, "growx, hidemode 3");
        configurationPanel.add(zStepSizeSpinner, "growx, hidemode 3, wrap");

        configurationPanel.add(feedRateLabel, "growx");
        configurationPanel.add(feedRateSpinner, "growx, wrap");
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
        JPanel space = new JPanel();
        space.setOpaque(false);
        xyzPanel.add(space, "grow");
        xyzPanel.add(getButtonFromEnum(JogPanelButtonEnum.BUTTON_XPOS), "grow");
        space = new JPanel();
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
                new Dimension(230, 0), // Scaling fonts to extra small
                new Dimension(250, 0)  // Scaling fonts to small
        );
        sizer.addListener(this);


        LongPressMouseListener longPressMouseListener = new LongPressMouseListener(LONG_PRESS_DELAY) {
            @Override
            protected void onMouseClicked(MouseEvent e) {
                JogPanelButtonEnum buttonEnum = getButtonEnumFromMouseEvent(e);
                listeners.forEach(a -> a.onButtonClicked(buttonEnum));
            }

            @Override
            protected void onMouseLongClicked(MouseEvent e) {

            }

            @Override
            protected void onMousePressed(MouseEvent e) {

            }

            @Override
            protected void onMouseRelease(MouseEvent e) {

            }

            @Override
            protected void onMouseLongPressed(MouseEvent e) {
                JogPanelButtonEnum buttonEnum = getButtonEnumFromMouseEvent(e);
                listeners.forEach(a -> a.onButtonLongPressed(buttonEnum));
            }

            @Override
            protected void onMouseLongRelease(MouseEvent e) {
                JogPanelButtonEnum buttonEnum = getButtonEnumFromMouseEvent(e);
                listeners.forEach(a -> a.onButtonLongReleased(buttonEnum));
            }
        };

        buttons.values().forEach(button -> button.addMouseListener(longPressMouseListener));

        xyStepSizeSpinner.addChangeListener(this);
        zStepSizeSpinner.addChangeListener(this);
        feedRateSpinner.addChangeListener(this);
    }

    /**
     * Finds the button enum based on the mouse event source
     *
     * @param mouseEvent the event that we want to extract the button enum from
     * @return the enum for the button
     */
    private JogPanelButtonEnum getButtonEnumFromMouseEvent(MouseEvent mouseEvent) {
        JButton releasedButton = (JButton) mouseEvent.getSource();
        return buttons.inverse().get(releasedButton);
    }

    /**
     * Returns the button from the button map using a button enum
     *
     * @param buttonEnum the button enum
     * @return the button
     */
    private JButton getButtonFromEnum(JogPanelButtonEnum buttonEnum) {
        return buttons.get(buttonEnum);
    }


    /**
     * Creates a image button with a text.
     *
     * @param baseUri            the base uri of the image
     * @param text               the text to be shown togheter with the icon
     * @param verticalAligment   Sets the vertical position of the text relative to the icon
     *                           and can have one of the following values
     *                           <ul>
     *                           <li>{@code SwingConstants.CENTER} (the default)
     *                           <li>{@code SwingConstants.TOP}
     *                           <li>{@code SwingConstants.BOTTOM}
     *                           </ul>
     * @param horisontalAligment Sets the horizontal position of the text relative to the
     *                           icon and can have one of the following values:
     *                           <ul>
     *                           <li>{@code SwingConstants.RIGHT}
     *                           <li>{@code SwingConstants.LEFT}
     *                           <li>{@code SwingConstants.CENTER}
     *                           <li>{@code SwingConstants.LEADING}
     *                           <li>{@code SwingConstants.TRAILING} (the default)
     *                           </ul>
     * @return the button
     */
    private JButton createImageButton(String baseUri, String text, int verticalAligment, int horisontalAligment) {
        JButton button = createImageButton(baseUri);
        button.setText(text);
        button.setVerticalTextPosition(verticalAligment);
        button.setHorizontalTextPosition(horisontalAligment);
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
        button.setFocusable(false);
        return button;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        buttons.values().forEach(button -> button.setEnabled(enabled));

        xyStepSizeSpinner.setEnabled(enabled);
        zStepSizeSpinner.setEnabled(enabled);
        feedRateSpinner.setEnabled(enabled);

        xyStepLabel.setEnabled(enabled);
        zStepLabel.setEnabled(enabled);
        feedRateLabel.setEnabled(enabled);
    }

    @Override
    public void onSizeChange(int size) {
        switch (size) {
            case 0:
                setFontSizeExtraSmall();
                break;
            case 1:
                setFontSizeSmall();
                break;
            default:
                setFontSizeNormal();
                break;
        }
    }

    private void setFontSizeExtraSmall() {
        JButton unitToggleButton = getButtonFromEnum(JogPanelButtonEnum.BUTTON_TOGGLE_UNIT);
        Font font = unitToggleButton.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        unitToggleButton.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.zStepLabel.setFont(font);
    }

    private void setFontSizeSmall() {
        JButton unitToggleButton = getButtonFromEnum(JogPanelButtonEnum.BUTTON_TOGGLE_UNIT);
        Font font = unitToggleButton.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        unitToggleButton.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.zStepLabel.setFont(font);
    }

    private void setFontSizeNormal() {
        JButton unitToggleButton = getButtonFromEnum(JogPanelButtonEnum.BUTTON_TOGGLE_UNIT);
        Font font = unitToggleButton.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        unitToggleButton.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.zStepLabel.setFont(font);
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
        } else if (e.getSource() == feedRateSpinner) {
            this.listeners.forEach(listener -> listener.onFeedRateChanged(feedRateSpinner.getValue().intValue()));
        }
    }
}
