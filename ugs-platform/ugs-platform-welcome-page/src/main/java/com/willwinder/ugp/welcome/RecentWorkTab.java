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
package com.willwinder.ugp.welcome;

import com.willwinder.ugp.welcome.content.AbstractTab;
import com.willwinder.ugp.welcome.content.Constants;
import com.willwinder.ugp.welcome.content.ContentSection;
import com.willwinder.ugp.welcome.content.JLinkButton;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class RecentWorkTab extends AbstractTab implements Constants {
  private static Logger logger = Logger.getLogger(RecentWorkTab.class.getName());
  private final BackendAPI backend;

  public RecentWorkTab() {
    super("Recent Work");
    backend = CentralLookup.getDefault().lookup(BackendAPI.class);
  }

  @Override
  protected JComponent buildContent() {
    JPanel recentFiles = new JPanel(new MigLayout("fillx, wrap 1"));
    JLabel fileLabel = new JLabel("Files");
    fileLabel.setFont(RecentWorkTab.CONTENT_HEADER_FONT);
    recentFiles.add(fileLabel);
    Collection<String> files = backend.getSettings().getRecentFiles();
    if (files != null && !files.isEmpty()) {
      for (String file : files) {
        final Path p = Paths.get(file);
        JLinkButton button = new JLinkButton(p.getFileName().toString());
        button.addActionListener(l -> GUIHelpers.openGcodeFile(p.toFile(), backend));
        recentFiles.add(button);
      }
    } else {
      logger.log(Level.INFO, "No files for recent work tab.");
      recentFiles.add(new JLabel("none yet."));
    }

    JPanel recentDirectories = new JPanel(new MigLayout("fillx, wrap 1"));
    JLabel dirLabel = new JLabel("Directories");
    dirLabel.setFont(RecentWorkTab.CONTENT_HEADER_FONT);
    recentDirectories.add(dirLabel);
    Collection<String> directories = backend.getSettings().getRecentDirectories();
    if (directories != null && !directories.isEmpty()) {
      for (String directory : directories) {
        JLinkButton button = new JLinkButton(directory);
        button.addActionListener(l -> {
            SwingHelpers
                .openFile(directory)
                .ifPresent((c) ->  GUIHelpers.openGcodeFile(c.getAbsoluteFile(), backend));
        });
        recentDirectories.add(button);
      }
    } else {
      logger.log(Level.INFO, "No directories for recent work tab.");
      recentDirectories.add(new JLabel("none yet."));
    }

    JPanel panel = new JPanel(new GridLayout(1,0));
    panel.setOpaque(false);
    panel.add(new ContentSection( recentDirectories, false ));
    panel.add(new ContentSection( recentFiles, false ));
    return panel;
  }
}
