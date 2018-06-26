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
package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.content.TabbedPane;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author wwinder
 */
public class WelcomePagePanel extends JPanel {
  private final static Color COLOR_TOP_START = new Color(220,235,255);
  private final static Color COLOR_TOP_END = new Color(255, 255, 255);
  private final static Color COLOR_BOTTOM_START = new Color(255, 255, 255);
  private final static Color COLOR_BOTTOM_END = new Color(241, 246, 252);

  public WelcomePagePanel(List<JComponent> tabs) {
    super( new GridBagLayout() );

    JComponent tabPane = new TabbedPane(tabs);
    tabPane.setBorder(BorderFactory.createEmptyBorder(10,15,15,15));
    tabPane.setOpaque(false);

    add( tabPane, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(27,0,0,0), 0, 0) );

    add( new JLabel(), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,0,0,0), 0, 0) );

  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    int width = getWidth();
    int height = getHeight();
    int gradientStop = height / 2;
    int bottomStart = gradientStop + gradientStop/2;

    g2d.setPaint(new GradientPaint(0, 0, COLOR_TOP_START, 0, gradientStop, COLOR_TOP_END));
    g2d.fillRect(0, 0, width, gradientStop);
    g2d.setPaint( COLOR_TOP_END );
    g2d.fillRect( 0, gradientStop, width, bottomStart );

    g2d.setPaint(new GradientPaint(0, bottomStart, COLOR_BOTTOM_START, 0, height, COLOR_BOTTOM_END));
    g2d.fillRect(0, bottomStart, width, height);
  }
}
