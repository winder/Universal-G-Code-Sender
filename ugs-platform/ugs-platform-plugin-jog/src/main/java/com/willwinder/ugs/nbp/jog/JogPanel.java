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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.uielements.helpers.MachineStatusFontManager;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.uielements.jog.StepSizeSpinner;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

/**
 * A panel for displaying jog controls
 *
 * @author Joacim Breiler
 */
public class JogPanel extends JPanel implements SteppedSizeManager.SteppedSizeChangeListener {

    /**
     * The minimum width and height of the jog buttons.
     */
    private static final int MINIMUM_BUTTON_SIZE = 52;

    private static final float FONT_SIZE_LABEL_SMALL = 8;
    private static final float FONT_SIZE_LABEL_MEDIUM = 10;
    private static final float FONT_SIZE_LABEL_LARGE = 14;

    /**
     * How long should the jog button be pressed before continuous
     * jog is activated in milliseconds
     */
    private static final int LONG_PRESS_DELAY = 500;

    /**
     * The inteval in milliseconds to send jog commands to the controller
     * when continuous jog is activated. This should be long enough so that
     * the queue isn't filled up.
     */
    private static final int LONG_PRESS_INTERVAL = 500;

    /**
     * How long should the step size be for continuous jog commands. These should be
     * long enough to keep the controller jogging before a new jog command is queued.
     */
    private static final int LONG_PRESS_STEP_SIZE = 100;

    private final JButton xposButton;
    private final JButton xnegButton;
    private final JButton yposButton;
    private final JButton ynegButton;
    private final JButton zposButton;
    private final JButton znegButton;
    private final JButton diagXnegYposButton;
    private final JButton diagXposYposButton;
    private final JButton diagXposYnegButton;
    private final JButton diagXnegYnegButton;
    private final JLabel feedRateLabel;
    private final JLabel xyStepLabel;
    private final JLabel zStepLabel;
    private final JButton unitToggleButton;
    private final StepSizeSpinner zStepSizeSpinner;
    private final StepSizeSpinner feedStepSizeSpinner;
    private final StepSizeSpinner xyStepSizeSpinner;
    private final JogService jogService;

    public JogPanel(JogService jogService) {
        this.jogService = jogService;

        String fontPath = "/resources/";
        // https://www.fontsquirrel.com
        String fontName = "OpenSans-Regular.ttf";
        InputStream is = getClass().getResourceAsStream(fontPath + fontName);
        Font font = MachineStatusFontManager.createFont(is, fontName).deriveFont(Font.PLAIN, FONT_SIZE_LABEL_LARGE);

        xposButton = createImageButton("icons/xpos.png", "X+", SwingConstants.CENTER, SwingConstants.LEFT);
        xnegButton = createImageButton("icons/xneg.png", "X-", SwingConstants.CENTER, SwingConstants.RIGHT);
        yposButton = createImageButton("icons/ypos.png", "Y+", SwingConstants.BOTTOM, SwingConstants.CENTER);
        ynegButton = createImageButton("icons/yneg.png", "Y-", SwingConstants.TOP, SwingConstants.CENTER);
        zposButton = createImageButton("icons/ypos.png", "Z+", SwingConstants.BOTTOM, SwingConstants.CENTER);
        znegButton = createImageButton("icons/yneg.png", "Z-", SwingConstants.TOP, SwingConstants.CENTER);

        diagXposYposButton = createImageButton("icons/diag-xpos-ypos.png");
        diagXnegYposButton = createImageButton("icons/diag-xneg-ypos.png");
        diagXposYnegButton = createImageButton("icons/diag-xpos-yneg.png");
        diagXnegYnegButton = createImageButton("icons/diag-xneg-yneg.png");

        feedStepSizeSpinner = new StepSizeSpinner();
        xyStepSizeSpinner = new StepSizeSpinner();
        zStepSizeSpinner = new StepSizeSpinner();

        // todo: could use a number of factory methods here to build similar stuff
        feedRateLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.feedRate").toUpperCase());
        xyStepLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.stepSizeXY").toUpperCase());
        zStepLabel = createSettingLabel(font, Localization.getString("platform.plugin.jog.stepSizeZ").toUpperCase());

        unitToggleButton = new JButton("--");
        unitToggleButton.setMinimumSize(new Dimension(MINIMUM_BUTTON_SIZE, MINIMUM_BUTTON_SIZE));
        unitToggleButton.setFocusable(false);

        SteppedSizeManager sizer = new SteppedSizeManager(this,
                new Dimension(230, 0), // Scaling fonts to extra small
                new Dimension(250, 0)  // Scaling fonts to small
        );
        sizer.addListener(this);

        initPanels();
        initListeners();

        setEnabled(jogService.canJog());
        setFeedRate(Double.valueOf(jogService.getFeedRate()).intValue());
        setStepSizeXY(jogService.getStepSizeXY());
        setStepSizeZ(jogService.getStepSizeZ());
        setUnit(jogService.getUnits());
        setUseStepSizeZ(jogService.useStepSizeZ());

        if (isDarkLaF()) {
            xposButton.setForeground(ThemeColors.LIGHT_BLUE);
            xnegButton.setForeground(ThemeColors.LIGHT_BLUE);
            yposButton.setForeground(ThemeColors.LIGHT_BLUE);
            ynegButton.setForeground(ThemeColors.LIGHT_BLUE);
            zposButton.setForeground(ThemeColors.LIGHT_BLUE);
            znegButton.setForeground(ThemeColors.LIGHT_BLUE);
            feedRateLabel.setForeground(ThemeColors.ORANGE);
            xyStepLabel.setForeground(ThemeColors.ORANGE);
            zStepLabel.setForeground(ThemeColors.ORANGE);
            unitToggleButton.setForeground(ThemeColors.LIGHT_BLUE);
        }
    }

    private static boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme"); //NOI18N
    }

    private JLabel createSettingLabel(Font font, String text) {
        JLabel label = new JLabel(text);
        label.setHorizontalTextPosition(SwingConstants.RIGHT);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setFont(font);
        return label;
    }

    public void setFeedRate(int feedRate) {
        feedStepSizeSpinner.setValue(String.valueOf(feedRate));
    }

    public void setStepSizeXY(double stepSize) {
        xyStepSizeSpinner.setValue(stepSize);
    }

    public void setStepSizeZ(double stepSize) {
        zStepSizeSpinner.setValue(stepSize);
    }

    public void setUnit(UnitUtils.Units unit) {
        unitToggleButton.setText(unit.name());
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
        configurationPanel.add(unitToggleButton, "grow, spany, wrap");

        configurationPanel.add(zStepLabel, "growx, hidemode 3");
        configurationPanel.add(zStepSizeSpinner, "growx, hidemode 3, wrap");

        configurationPanel.add(feedRateLabel, "growx");
        configurationPanel.add(feedStepSizeSpinner, "growx, wrap");
        return configurationPanel;
    }

    private JPanel createXYZPanel() {
        JPanel xyzPanel = new JPanel();
        xyzPanel.setLayout(new MigLayout("fill, wrap 4, inset 0, gap 2", "[25%, center][25%, center][25%, center][25%, center]", "[33%, center][33%, center][33%, center]"));

        xyzPanel.add(diagXnegYposButton, "grow");
        xyzPanel.add(yposButton, "grow");
        xyzPanel.add(diagXposYposButton, "grow");
        xyzPanel.add(zposButton, "grow");

        xyzPanel.add(xnegButton, "grow");
        JPanel space = new JPanel();
        space.setOpaque(false);
        xyzPanel.add(space, "grow");
        xyzPanel.add(xposButton, "grow");
        space = new JPanel();
        space.setOpaque(false);
        xyzPanel.add(space, "grow");

        xyzPanel.add(diagXnegYnegButton, "grow");
        xyzPanel.add(ynegButton, "grow");
        xyzPanel.add(diagXposYnegButton, "grow");
        xyzPanel.add(znegButton, "grow");

        return xyzPanel;
    }

    private void initListeners() {
        xposButton.addMouseListener(buildJogButtonListener(1, 0, 0));
        xnegButton.addMouseListener(buildJogButtonListener(-1, 0, 0));
        yposButton.addMouseListener(buildJogButtonListener(0, 1, 0));
        ynegButton.addMouseListener(buildJogButtonListener(0, -1, 0));
        zposButton.addMouseListener(buildJogButtonListener(0, 0, 1));
        znegButton.addMouseListener(buildJogButtonListener(0, 0, -1));
        diagXnegYnegButton.addMouseListener(buildJogButtonListener(-1, -1, 0));
        diagXnegYposButton.addMouseListener(buildJogButtonListener(-1, 1, 0));
        diagXposYnegButton.addMouseListener(buildJogButtonListener(1, -1, 0));
        diagXposYposButton.addMouseListener(buildJogButtonListener(1, 1, 0));

        unitToggleButton.addActionListener((event) -> {
            if (jogService.getUnits() == UnitUtils.Units.MM) {
                jogService.setUnits(UnitUtils.Units.INCH);
            } else {
                jogService.setUnits(UnitUtils.Units.MM);
            }
            unitToggleButton.setText(jogService.getUnits().name());
        });

        xyStepSizeSpinner.addChangeListener((event) -> jogService.setStepSize(xyStepSizeSpinner.getValue()));
        zStepSizeSpinner.addChangeListener((event) -> jogService.setStepSizeZ(zStepSizeSpinner.getValue()));
        feedStepSizeSpinner.addChangeListener((event) -> jogService.setFeedRate(feedStepSizeSpinner.getValue()));
    }

    /**
     * Creates a jog button listener that will initialize jog actions to a specific direction.
     *
     * @param x should jogging on x-axis be activated, can have values (-1, 0, 1)
     * @param y should jogging on y-axis be activated, can have values (-1, 0, 1)
     * @param z should jogging on z-axis be activated, can have values (-1, 0, 1)
     * @return a jog button listener
     */
    private JogButtonListener buildJogButtonListener(int x, int y, int z) {
        return new JogButtonListener(LONG_PRESS_DELAY, LONG_PRESS_INTERVAL,
                () -> {
                    if (z != 0) {
                        jogService.adjustManualLocationZ(z);
                    }

                    if (x != 0 || y != 0) {
                        jogService.adjustManualLocationXY(x, y);
                    }
                },
                () -> jogService.adjustManualLocation(x, y, z, LONG_PRESS_STEP_SIZE),
                jogService::cancelJog
        );
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
        unitToggleButton.setEnabled(enabled);

        xyStepSizeSpinner.setEnabled(enabled);
        zStepSizeSpinner.setEnabled(enabled);
        feedStepSizeSpinner.setEnabled(enabled);

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
        Font font = this.unitToggleButton.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.unitToggleButton.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_SMALL);
        this.zStepLabel.setFont(font);
    }

    private void setFontSizeSmall() {
        Font font = this.unitToggleButton.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.unitToggleButton.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_MEDIUM);
        this.zStepLabel.setFont(font);
    }

    private void setFontSizeNormal() {
        Font font = this.unitToggleButton.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.unitToggleButton.setFont(font);

        font = this.feedRateLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.feedRateLabel.setFont(font);

        font = this.xyStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.xyStepLabel.setFont(font);

        font = this.zStepLabel.getFont().deriveFont(FONT_SIZE_LABEL_LARGE);
        this.zStepLabel.setFont(font);
    }
}
