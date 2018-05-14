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
package com.willwinder.ugp.content;

import com.willwinder.ugp.helper.StretchIcon;
import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

/**
 *
 * @author wwinder
 */
public class GettingStartedTab extends AbstractTab {
  final String contentString;

  /*
  private static Image connect = ImageUtilities.loadImage("com/willwinder/ugp/resources/connect.png", true);
  private static Image open = ImageUtilities.loadImage("com/willwinder/ugp/resources/open.png", true);
  private static Image run = ImageUtilities.loadImage("com/willwinder/ugp/resources/run.png", true);
  */

  private static Image connect = Toolkit.getDefaultToolkit().getImage(
      GettingStartedTab.class.getClassLoader().getResource("com/willwinder/ugp/resources/connect.png"));
  private static Image open = Toolkit.getDefaultToolkit().getImage(
      GettingStartedTab.class.getClassLoader().getResource("com/willwinder/ugp/resources/open.png"));
  private static Image run = Toolkit.getDefaultToolkit().getImage(
      GettingStartedTab.class.getClassLoader().getResource("com/willwinder/ugp/resources/run.png"));

  public GettingStartedTab() {
    super("Getting Started");
    this.contentString = "blah";
  }

  @Override
  protected JComponent buildContent() {
    JPanel contentPanel = new JPanel(new MigLayout("fill, wrap 1"));

    contentPanel.add(new JLabel(new ImageIcon(connect)));
    contentPanel.add(new JLabel("Configure your controller and connect."));

    contentPanel.add(new JLabel(new ImageIcon(open)));
    contentPanel.add(new JLabel("Open a gcode file."));

    contentPanel.add(new JLabel(new ImageIcon(run)));
    contentPanel.add(new JLabel("Stream the file with play/pause/stop buttons."));

    JScrollPane scroll = new JScrollPane(contentPanel);
    return new ContentSection( scroll, false);
  }
  
}

