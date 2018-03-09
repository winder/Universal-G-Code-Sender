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

import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.components.Button;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.MachineStatusFontManager;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Font;
import java.awt.Dimension;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * A panel for displaying jog controls
 *
 * @author Joacim Breiler
 */
public class JogPanel extends JPanel implements SteppedSizeManager.SteppedSizeChangeListener {
    private static final int COMMON_RADIUS = 7;
    private static final float FONT_SIZE_LABEL_SMALL = 10;
    private static final float FONT_SIZE_LABEL_MEDIUM = 14;
    private static final float FONT_SIZE_LABEL_LARGE = 17;

    private static final float FONT_SIZE_VALUE_SMALL = 17;
    private static final float FONT_SIZE_VALUE_MEDIUM = 19;

    private final Button xposButton;
    private final Button xnegButton;
    private final Button yposButton;
    private final Button ynegButton;
    private final Button zposButton;
    private final Button znegButton;
    private final Button diagXnegYposButton;
    private final Button diagXposYposButton;
    private final Button diagXposYnegButton;
    private final Button diagXnegYnegButton;
    private final JLabel xyLabel;
    private final JLabel zLabel;
    private final JLabel feedRateLabel;
    private final JLabel xyStepLabel;
    private final JLabel zStepLabel;
    private final JLabel feedRateValue;
    private final JLabel xyStepValue;
    private final JLabel zStepValue;
    private final JLabel unitToggleLabel;
    private final Button unitToggleButton;
    private final Button feedIncButton;
    private final Button feedDecButton;
    private final Button stepXYIncButton;
    private final Button stepXYDecButton;
    private final Button stepZIncButton;
    private final Button stepZDecButton;
    private Set<JogPanelListener> listeners;

    public JogPanel() {
        String fontPath = "/resources/";
        // https://www.fontsquirrel.com
        String fontName = "OpenSans-Regular.ttf";
        InputStream is = getClass().getResourceAsStream(fontPath + fontName);
        Font font = MachineStatusFontManager.createFont(is, fontName);
        Font labelFontSmall = font.deriveFont(Font.PLAIN, FONT_SIZE_LABEL_MEDIUM);
        Font labelFontLarge = font.deriveFont(Font.PLAIN, FONT_SIZE_LABEL_LARGE);
        fontName = "LED.ttf";
        is = getClass().getResourceAsStream(fontPath + fontName);
        font = MachineStatusFontManager.createFont(is, fontName);
        Font valueFont = font.deriveFont(Font.PLAIN, FONT_SIZE_VALUE_MEDIUM);

        xposButton = createImageButton("icons/xpos.png");
        xnegButton = createImageButton("icons/xneg.png");
        yposButton = createImageButton("icons/ypos.png");
        ynegButton = createImageButton("icons/yneg.png");
        zposButton = createImageButton("icons/ypos.png");
        znegButton = createImageButton("icons/yneg.png");

        diagXposYposButton = createImageButton("icons/diag-xpos-ypos.png");
        diagXnegYposButton = createImageButton("icons/diag-xneg-ypos.png");
        diagXposYnegButton = createImageButton("icons/diag-xpos-yneg.png");
        diagXnegYnegButton = createImageButton("icons/diag-xneg-yneg.png");

        feedIncButton = createImageButton("icons/xpos.png");
        feedDecButton = createImageButton("icons/xneg.png");
        stepXYIncButton = createImageButton("icons/xpos.png");
        stepXYDecButton = createImageButton("icons/xneg.png");
        stepZIncButton = createImageButton("icons/xpos.png");
        stepZDecButton = createImageButton("icons/xneg.png");

        // todo: could use a number of factory methods here to build similar stuff
        xyLabel = new JLabel("X/Y");
        xyLabel.setForeground(ThemeColors.ORANGE);
        xyLabel.setFont(labelFontLarge);
        xyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        zLabel = new JLabel("Z");
        zLabel.setForeground(ThemeColors.ORANGE);
        zLabel.setFont(labelFontLarge);
        zLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // todo: i18n
        feedRateLabel = new JLabel("FEED RATE");
        feedRateLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        feedRateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        feedRateLabel.setForeground(ThemeColors.ORANGE);
        feedRateLabel.setFont(labelFontSmall);

        feedRateValue = new JLabel("--");
        feedRateValue.setHorizontalTextPosition(SwingConstants.CENTER);
        feedRateValue.setHorizontalAlignment(SwingConstants.CENTER);
        feedRateValue.setForeground(ThemeColors.ORANGE);
        feedRateValue.setFont(valueFont);

        // todo: i18n
        xyStepLabel = new JLabel("X/Y STEP");
        xyStepLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        xyStepLabel.setHorizontalAlignment(SwingConstants.CENTER);
        xyStepLabel.setForeground(ThemeColors.ORANGE);
        xyStepLabel.setFont(labelFontSmall);

        // todo: i18n
        zStepLabel = new JLabel("Z STEP");
        zStepLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        zStepLabel.setHorizontalAlignment(SwingConstants.CENTER);
        zStepLabel.setForeground(ThemeColors.ORANGE);
        zStepLabel.setFont(labelFontSmall);

        xyStepValue = new JLabel("--");
        xyStepValue.setHorizontalTextPosition(SwingConstants.CENTER);
        xyStepValue.setHorizontalAlignment(SwingConstants.CENTER);
        xyStepValue.setForeground(ThemeColors.ORANGE);
        xyStepValue.setFont(valueFont);

        zStepValue = new JLabel("--");
        zStepValue.setHorizontalTextPosition(SwingConstants.CENTER);
        zStepValue.setHorizontalAlignment(SwingConstants.CENTER);
        zStepValue.setForeground(ThemeColors.ORANGE);
        zStepValue.setFont(valueFont);

        unitToggleLabel = new JLabel("--");
        unitToggleLabel.setForeground(ThemeColors.LIGHT_BLUE);
        unitToggleLabel.setFont(labelFontLarge);
        unitToggleButton = new Button(unitToggleLabel);
        unitToggleButton.addClickListener(() -> {
            if (unitToggleLabel.getText().equalsIgnoreCase(UnitUtils.Units.MM.name())) {
                unitToggleLabel.setText(UnitUtils.Units.INCH.name().toUpperCase());
            } else {
                unitToggleLabel.setText(UnitUtils.Units.MM.name().toUpperCase());
            }
        });

        listeners = new HashSet<>();

        SteppedSizeManager sizer = new SteppedSizeManager(this,
                new Dimension(250, 250), // Scaling fonts to smaller
                new Dimension(260, 0),  // Shortens text
                new Dimension(280, 0)); // Full scale of everything
        sizer.addListener(this);

        initComponents();
        initListeners();
    }

    private void initComponents() {
        String debug = "";
        //debug = "debug, ";
        setLayout(new MigLayout(debug + "fill, inset 5, gap 4", "[73%][27%]"));

        JPanel movementPanel = createGroupPanel();
        movementPanel.setLayout(new MigLayout(debug + "fill, inset 0, gap 0", "[75%][25%]"));
        movementPanel.add(createXYJogPanel(), "grow");
        movementPanel.add(createZJogPanel(), "grow");
        add(movementPanel, "grow, wrap, spanx 2");

        add(createJogXYSizePanel(), "growx");
        add(createUnitPanel(), "grow, wrap, spany 3");
        add(createJogZSizePanel(), "growx, wrap");
        add(createFeedRatePanel(), "growx");
    }

    private void initListeners() {
        xposButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_XPOS));
        xnegButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_XNEG));
        yposButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_YPOS));
        ynegButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_YNEG));
        zposButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_ZPOS));
        znegButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_ZNEG));
        diagXnegYnegButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_DIAG_XNEG_YNEG));
        diagXnegYposButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_DIAG_XNEG_YPOS));
        diagXposYnegButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_DIAG_XPOS_YNEG));
        diagXposYposButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_DIAG_XPOS_YPOS));
        unitToggleButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_TOGGLE_UNIT));
        feedIncButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_FEED_INC));
        feedDecButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_FEED_DEC));
        stepXYIncButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_XY_STEP_INC));
        stepXYDecButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_XY_STEP_DEC));
        stepZIncButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_Z_STEP_INC));
        stepZDecButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_Z_STEP_DEC));
    }

    private Component createUnitPanel() {
        RoundedPanel panel = createGroupPanel();
        panel.setLayout(new MigLayout("fill, inset 7"));
        panel.add(unitToggleButton, "grow");
        return panel;
    }

    private Component createFeedRatePanel() {
        return createParameterPanel(feedDecButton, feedRateLabel, feedRateValue, feedIncButton);
    }

    private Component createJogXYSizePanel() {
        return createParameterPanel(stepXYDecButton, xyStepLabel, xyStepValue, stepXYIncButton);
    }

    private Component createJogZSizePanel() {
        return createParameterPanel(stepZDecButton, zStepLabel, zStepValue, stepZIncButton);
    }

    private Component createParameterPanel(Component left, Component label, Component value, Component right) {
        RoundedPanel panel = createGroupPanel();
        panel.setLayout(new MigLayout("fill, inset 7", "[][grow][grow][]"));

        panel.add(left);
        panel.add(label, "growy, al right, pad 2 0 0 0");
        panel.add(value, "growy, pad 2 0 0 0");
        panel.add(right);

        return panel;
    }

    private Component createXYJogPanel() {
        JPanel xyPanel = new JPanel();
        xyPanel.setOpaque(false);
        xyPanel.setLayout(new MigLayout("fill, wrap 3, inset 7, gap 7", "[33%][33%][33%]", "[33%][33%][33%]"));

        xyPanel.add(diagXnegYposButton, "grow");
        xyPanel.add(yposButton, "grow");
        xyPanel.add(diagXposYposButton, "grow");

        xyPanel.add(xnegButton, "grow");
        xyPanel.add(xyLabel, "grow");
        xyPanel.add(xposButton, "grow");

        xyPanel.add(diagXnegYnegButton, "grow");
        xyPanel.add(ynegButton, "grow");
        xyPanel.add(diagXposYnegButton, "grow");

        return xyPanel;
    }

    private Component createZJogPanel() {
        JPanel zPanel = new JPanel();
        zPanel.setOpaque(false);
        zPanel.setLayout(new MigLayout("fill, wrap1, inset 7", "", "[33%][33%][33%]"));

        zPanel.add(zposButton, "grow");
        zPanel.add(zLabel, "grow");
        zPanel.add(znegButton, "grow");

        return zPanel;
    }

    private RoundedPanel createGroupPanel() {
        RoundedPanel panel = new RoundedPanel(COMMON_RADIUS);
        panel.setBackground(ThemeColors.VERY_DARK_GREY);
        panel.setForeground(ThemeColors.LIGHT_BLUE);
        return panel;
    }

    private Button createImageButton(String baseUri) {
        return new Button(ImageUtilities.loadImageIcon(baseUri, false));
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        xposButton.setEnabled(enabled);
        xnegButton.setEnabled(enabled);
        yposButton.setEnabled(enabled);
        ynegButton.setEnabled(enabled);
        zposButton.setEnabled(enabled);
        znegButton.setEnabled(enabled);

        diagXnegYposButton.setEnabled(enabled);
        diagXposYposButton.setEnabled(enabled);
        diagXposYnegButton.setEnabled(enabled);
        diagXnegYnegButton.setEnabled(enabled);

        stepXYDecButton.setEnabled(enabled);
        stepXYIncButton.setEnabled(enabled);
        stepZDecButton.setEnabled(enabled);
        stepZIncButton.setEnabled(enabled);

        feedDecButton.setEnabled(enabled);
        feedIncButton.setEnabled(enabled);

        unitToggleButton.setEnabled(enabled);
    }

    public void addJogPanelListener(JogPanelListener listener) {
        listeners.add(listener);
    }

    public void setJogFeedRate(int jogFeedRate) {
        feedRateValue.setText(String.valueOf(jogFeedRate));
    }

    public void setXyStepLength(double xyStepLength) {
        xyStepValue.setText(String.valueOf(xyStepLength));
    }

    public void setZStepLength(double zStepLength) {
        zStepValue.setText(String.valueOf(zStepLength));
    }

    public void setUnit(UnitUtils.Units unit) {
        unitToggleLabel.setText(unit.name().toUpperCase());
    }

    @Override
    public void onSizeChange(int size) {
        switch (size) {
            case 0:
                this.feedRateLabel.setText("F.RATE");
                setSmallFontSizes();
                break;
            case 1:
                this.feedRateLabel.setText("F.RATE");
                setNormalFontSizes();
                break;
            default:
                this.feedRateLabel.setText("FEED RATE");
                setNormalFontSizes();
                break;
        }
    }

    private void setSmallFontSizes() {
        Font font = this.xyLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.xyLabel.setFont(font);

        font = this.zLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.zLabel.setFont(font);

        font = this.unitToggleLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.unitToggleLabel.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.zStepLabel.setFont(font);

        font = this.feedRateValue.getFont().deriveFont(FONT_SIZE_VALUE_SMALL);
        this.feedRateValue.setFont(font);

        font = this.xyStepValue.getFont().deriveFont(FONT_SIZE_VALUE_SMALL);
        this.xyStepValue.setFont(font);

        font = this.zStepValue.getFont().deriveFont(FONT_SIZE_VALUE_SMALL);
        this.zStepValue.setFont(font);
    }

    private void setNormalFontSizes() {
        Font font;
        font = this.xyLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.xyLabel.setFont(font);

        font = this.zLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.zLabel.setFont(font);

        font = this.unitToggleLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.unitToggleLabel.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.zStepLabel.setFont(font);

        font = this.feedRateValue.getFont().deriveFont(FONT_SIZE_VALUE_MEDIUM);
        this.feedRateValue.setFont(font);

        font = this.xyStepValue.getFont().deriveFont(FONT_SIZE_VALUE_MEDIUM);
        this.xyStepValue.setFont(font);

        font = this.zStepValue.getFont().deriveFont(FONT_SIZE_VALUE_MEDIUM);
        this.zStepValue.setFont(font);
    }
}
