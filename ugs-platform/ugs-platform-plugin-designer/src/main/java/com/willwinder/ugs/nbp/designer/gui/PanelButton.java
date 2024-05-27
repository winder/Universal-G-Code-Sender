/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Joacim Breiler
 */
public class PanelButton extends JButton {

    public static final float TITLE_SCALE = 0.6f;
    public static final float TEXT_SCALE = 1.0f;
    private final JLabel textLabel;
    private final JLabel titleLabel;

    public PanelButton(String title, String text) {
        super();
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(100, 100));
        titleLabel = new JLabel(title);
        Font font = titleLabel.getFont();
        font = font.deriveFont(font.getSize() * TITLE_SCALE);
        titleLabel.setFont(font);
        add(titleLabel, BorderLayout.NORTH);

        textLabel = new JLabel(text);
        font = textLabel.getFont();
        font = font.deriveFont(font.getSize() * TEXT_SCALE);
        textLabel.setFont(font);
        add(textLabel, BorderLayout.CENTER);
    }

    @Override
    public void setText(String text) {
        this.textLabel.setText(text);
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }
}
