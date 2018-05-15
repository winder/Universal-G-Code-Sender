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

import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class GettingStartedTab extends AbstractTab implements Constants {
  final String contentString;

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

  private static String line1 = "<html>Welcome to Universal Gcode Sender! If this is your first time using UGS thanks for giving it a try. This window is here to help you get started, once you're comfortable with the basic sending functionality please explore the other tabs to learn about some of the other features that UGS has to offer.</html>";

  private static String openLine = "<html>Open your Gcode file from the 'File' menu. Once you load a file there are a number of things you can do with it: inspect the toolpaths from the Visualizer, edit them by selecting edit from the 'File' menu, see the number of rows in the program in the UGS status area. UGS is a great way to preview your toolpaths even if you aren't ready to run the program yet!</html>";

  private static String connectLine1 = "<html>When you are ready to connect to your machine, the serial connection can be configured in the toolbar at the top of the program. Choose your firmware from the combo box and configure the serial options accordingly. If you haven't configured your controller yet you may be interested in the configuration wizard found in the 'Machine' menu.</html>";
  private static String connectLine2 = "<html>Once connected you can start using UGS to control your machine. Some good places to start are the Jog Controller, manually sending commands in the console, or working with the coordinate system in the DRO. More information about these features can be found in the other tabs and at http://winder.github.io/ugs_website/</html>";

  private static String runLine = "<html>Once you have loaded a file and established a connection, the program may be run. These icons in the toolbar can be used to stream, pause, and stop the program.</html>";

  @Override
  protected JComponent buildContent() {
    JPanel contentPanel = new JPanel(new MigLayout("fill, wrap 1"));

    contentPanel.add(getLabel(SECTION_HEADER_FONT, line1));

    JPanel openPanel = new JPanel(new MigLayout("fill"));
    openPanel.add(getLabel(SECTION_HEADER_FONT, openLine), "al left");
    openPanel.add(new JLabel(new ImageIcon(open)), "al right");
    contentPanel.add(openPanel);

    JPanel connectPanel = new JPanel(new MigLayout("fill, wrap 1"));
    connectPanel.add(new JLabel(new ImageIcon(connect)), "al left");
    connectPanel.add(getLabel(SECTION_HEADER_FONT, connectLine1), "shrink, al left");
    connectPanel.add(getLabel(SECTION_HEADER_FONT, connectLine2), "shrink, al left");
    contentPanel.add(connectPanel);

    JPanel runPanel = new JPanel(new MigLayout("fill"));
    runPanel.add(new JLabel(new ImageIcon(run)), "al right");
    runPanel.add(getLabel(SECTION_HEADER_FONT, runLine), "al left");
    contentPanel.add(runPanel);

    return new ContentSection( contentPanel, false);
  }

  private JLabel getLabel(Font font, String message) {
    JLabel label = new JLabel(message);
    label.setFont(font);
    return label;
  }
  
}

