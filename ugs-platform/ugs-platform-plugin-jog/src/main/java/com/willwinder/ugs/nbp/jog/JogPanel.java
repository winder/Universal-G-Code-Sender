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
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * A panel for displaying jog controls
 *
 * @author Joacim Breiler
 */
public class JogPanel extends JPanel {
    private static final int COMMON_RADIUS = 7;
    private static final int FONT_SIZE_SMALL = 12;

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
    private final JLabel unitToggleLabel;
    private final Button unitToggleButton;
    private final Button feedIncButton;
    private final Button feedDecButton;
    private final Button stepIncButton;
    private final Button stepDecButton;
    private java.util.List<JogPanelListener> listeners;

    public JogPanel() {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 30);

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
        stepIncButton = createImageButton("icons/xpos.png");
        stepDecButton = createImageButton("icons/xneg.png");

        xyLabel = new JLabel("X/Y");
        xyLabel.setForeground(ThemeColors.LIGHT_BLUE);
        xyLabel.setFont(font.deriveFont(Font.PLAIN, FONT_SIZE_SMALL));
        xyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        zLabel = new JLabel("Z");
        zLabel.setForeground(ThemeColors.LIGHT_BLUE);
        zLabel.setFont(font.deriveFont(Font.PLAIN, FONT_SIZE_SMALL));
        zLabel.setHorizontalAlignment(SwingConstants.CENTER);

        feedRateLabel = new JLabel();
        feedRateLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        feedRateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        feedRateLabel.setForeground(ThemeColors.LIGHT_BLUE);
        feedRateLabel.setFont(font.deriveFont(Font.PLAIN, FONT_SIZE_SMALL));

        xyStepLabel = new JLabel();
        xyStepLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        xyStepLabel.setHorizontalAlignment(SwingConstants.CENTER);
        xyStepLabel.setForeground(ThemeColors.LIGHT_BLUE);
        xyStepLabel.setFont(font.deriveFont(Font.PLAIN, FONT_SIZE_SMALL));

        unitToggleLabel = new JLabel();
        unitToggleLabel.setForeground(ThemeColors.LIGHT_BLUE);
        unitToggleLabel.setFont(font.deriveFont(Font.PLAIN, FONT_SIZE_SMALL));
        unitToggleButton = new Button(unitToggleLabel);
        unitToggleButton.addClickListener(() -> {
            if (unitToggleLabel.getText().equalsIgnoreCase(UnitUtils.Units.MM.name())) {
                unitToggleLabel.setText(UnitUtils.Units.INCH.name().toLowerCase());
            } else {
                unitToggleLabel.setText(UnitUtils.Units.MM.name().toLowerCase());
            }
        });

        listeners = new ArrayList<>();

        initComponents();
        initListeners();
    }

    private void initComponents() {
        String debug = "";
        //debug = "debug, ";
        setLayout(new MigLayout(debug + "fill, inset 5", "", "[90%][5%][5%]"));

        add(createControlsRow(), "grow, wrap");
        add(createJogXYSizeRow(), "grow, wrap");
        add(createFeedRateRow(), "grow, wrap");
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
        stepIncButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_STEP_INC));
        stepDecButton.addClickListener(new JogPanelListenerProxy(listeners, JogPanelButtonEnum.BUTTON_STEP_DEC));
    }

    private Component createFeedRateRow() {
        RoundedPanel stepPanel = new RoundedPanel(COMMON_RADIUS);
        stepPanel.setLayout(new MigLayout("fill, inset 7", "[][grow][]", ""));
        stepPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        stepPanel.setForeground(ThemeColors.LIGHT_BLUE);

        stepPanel.add(feedDecButton, "grow");
        stepPanel.add(feedRateLabel, "grow");
        stepPanel.add(feedIncButton, "grow");
        return stepPanel;
    }

    private Component createJogXYSizeRow() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.add(xyStepLabel);
        panel.add(unitToggleButton, "aligny center, grow");

        RoundedPanel stepPanel = new RoundedPanel(COMMON_RADIUS);
        stepPanel.setLayout(new MigLayout("fill, inset 7", "[][grow][]", ""));
        stepPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        stepPanel.setForeground(ThemeColors.LIGHT_BLUE);

        stepPanel.add(stepDecButton, "grow");
        stepPanel.add(panel, "grow");
        stepPanel.add(stepIncButton, "grow");
        return stepPanel;
    }

    private Component createControlsRow() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new MigLayout("fill, inset 0", "[75%][25%]"));

        RoundedPanel xyPanel = new RoundedPanel(COMMON_RADIUS);
        xyPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        xyPanel.setForeground(ThemeColors.LIGHT_BLUE);
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
        panel.add(xyPanel, "grow");

        RoundedPanel zPanel = new RoundedPanel(COMMON_RADIUS);
        zPanel.setLayout(new MigLayout("fill, wrap1, inset 7", "", "[33%][33%][33%]"));
        zPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        zPanel.setForeground(ThemeColors.LIGHT_BLUE);
        zPanel.add(zposButton, "grow");
        zPanel.add(zLabel, "grow");
        zPanel.add(znegButton, "grow");
        panel.add(zPanel, "grow");
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

        stepDecButton.setEnabled(enabled);
        stepIncButton.setEnabled(enabled);
        feedDecButton.setEnabled(enabled);
        feedIncButton.setEnabled(enabled);

        zLabel.setEnabled(enabled);
        xyLabel.setEnabled(enabled);
        xyStepLabel.setEnabled(enabled);
        feedRateLabel.setEnabled(enabled);
        unitToggleButton.setEnabled(enabled);
    }

    public void addJogPanelListener(JogPanelListener listener) {
        listeners.add(listener);
    }

    public void setJogFeedRate(int jogFeedRate) {
        feedRateLabel.setText("<html><center>Feed rate<br><b>" + jogFeedRate + " steps/min</b></center></html>");
    }

    public void setXyStepLength(double xyStepLength) {
        xyStepLabel.setText("<html><center>Step length<br><b>" + xyStepLength + "</b></center></html>");
    }

    public void setUnit(UnitUtils.Units unit) {
        unitToggleLabel.setText(unit.name().toLowerCase());
    }
}
