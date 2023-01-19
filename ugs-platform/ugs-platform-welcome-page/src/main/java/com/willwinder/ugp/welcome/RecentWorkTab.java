/*
    Copyright 2018-2022 Will Winder

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
import com.willwinder.ugp.welcome.content.JLinkButton;
import com.willwinder.ugs.nbp.core.actions.OpenAction;
import com.willwinder.ugs.nbp.core.actions.OpenFileAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wwinder
 */
public class RecentWorkTab extends AbstractTab {
    private static final Logger LOGGER = Logger.getLogger(RecentWorkTab.class.getName());
    private final transient BackendAPI backend;

    public RecentWorkTab() {
        super("Recent Work");
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
    }

    @Override
    protected JComponent buildContent() {
        JPanel recentFiles = new JPanel(new MigLayout("fillx, wrap 1"));
        recentFiles.setOpaque(false);
        JLabel fileLabel = new JLabel("Files");
        fileLabel.setFont(Constants.CONTENT_HEADER_FONT);
        fileLabel.setForeground(Constants.COLOR_TEXT);
        recentFiles.add(fileLabel);

        Collection<String> files = backend.getSettings().getRecentFiles();
        if (files != null && !files.isEmpty()) {
            for (String file : files) {
                final Path p = Paths.get(file);
                if (p.toFile().exists()) {
                    recentFiles.add(createFileButton(p));
                }
            }
        } else {
            LOGGER.log(Level.INFO, "No files for recent work tab.");
            recentFiles.add(new JLabel("none yet."));
        }

        JPanel recentDirectories = new JPanel(new MigLayout("fillx, wrap 1"));
        recentDirectories.setOpaque(false);
        JLabel dirLabel = new JLabel("Directories");
        dirLabel.setFont(Constants.CONTENT_HEADER_FONT);
        dirLabel.setForeground(Constants.COLOR_TEXT);
        recentDirectories.add(dirLabel);

        Collection<String> directories = backend.getSettings().getRecentDirectories();
        if (directories != null && !directories.isEmpty()) {
            for (String directory : directories) {
                recentDirectories.add(createOpenDirectoryButton(directory));
            }
        } else {
            LOGGER.log(Level.INFO, "No directories for recent work tab.");
            recentDirectories.add(new JLabel("none yet."));
        }

        JPanel panel = new JPanel(new GridLayout(1, 0));
        panel.setOpaque(true);
        panel.add(recentDirectories);
        panel.add(recentFiles);
        return panel;
    }

    private JLinkButton createOpenDirectoryButton(String directory) {
        JLinkButton button = new JLinkButton(directory);
        button.setToolTipText(directory);
        button.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        if (isDarkLaF()) {
            button.setLinkColor(ThemeColors.LIGHT_BLUE_GREY);
        }

        button.addActionListener(l -> {
            OpenAction openAction = new OpenAction(directory);
            openAction.actionPerformed(new ActionEvent(this, 0, ""));
        });
        return button;
    }

    private JLinkButton createFileButton(Path p) {
        JLinkButton button = new JLinkButton(p.getFileName().toString());
        button.setToolTipText(p.toFile().getAbsolutePath());
        button.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        if (isDarkLaF()) {
            button.setLinkColor(ThemeColors.LIGHT_BLUE_GREY);
        }

        button.addActionListener(l -> new OpenFileAction(p.toFile()).actionPerformed(null));
        return button;
    }
}
