/*
    Copyright 2016-2024 Will Winder

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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Displays a list of files and directories in a given directory.
 *
 * @author andrewmurraydavid
 */
public final class FileBrowserPanel extends JPanel implements UGSEventListener {
    private final transient BackendAPI backend;
    private final JTree fileTree;
    private File currentFile;
    private final JTextField currentPathField;
    private final JCheckBox showHiddenCheckBox;

    public FileBrowserPanel(BackendAPI backend) {
        this.backend = backend;
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new BorderLayout());
        currentPathField = new JTextField();
        northPanel.add(currentPathField, BorderLayout.CENTER);

        JButton goButton = new JButton("Go");
        northPanel.add(goButton, BorderLayout.EAST);

        showHiddenCheckBox = new JCheckBox("Show Hidden", false);
        northPanel.add(showHiddenCheckBox, BorderLayout.SOUTH);

        add(northPanel, BorderLayout.NORTH);

        File initialDirectory = new File(System.getProperty("user.home"));

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(new FileNode(initialDirectory));
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);

        fileTree.setRootVisible(true);
        fileTree.setShowsRootHandles(true);
        JScrollPane treeScroll = new JScrollPane(fileTree);
        add(treeScroll, BorderLayout.CENTER);

        setDirectory(backend != null ?
                        new File(backend.getSettings().getLastWorkingDirectory()) :
                        initialDirectory
                    );

        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                        FileNode fileNode = (FileNode) selectedNode.getUserObject();
                        if (fileNode.displayName.equals("..")) {
                            DefaultMutableTreeNode upperNode = (DefaultMutableTreeNode) selectedNode.getParent();
                            File upperPath = ((FileNode) upperNode.getUserObject()).getFile();
                            File parentFile = upperPath.getParentFile();
                            setDirectory(parentFile);
                        } else if (fileNode.getFile().isDirectory()) {
                            setDirectory(fileNode.getFile());
                        } else {
                            File gcodeFile = fileNode.getFile();
                            GUIHelpers.openGcodeFile(gcodeFile, backend);
                        }
                    }
                }
            }
        });

        fileTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) {
                TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                FileNode fileNode = (FileNode) node.getUserObject();

                if (fileNode.isDirectory()) {
                    node.removeAllChildren();
                    createChildren(node, fileNode.getFile(), showHiddenCheckBox.isSelected());
                    ((DefaultTreeModel) fileTree.getModel()).nodeStructureChanged(node);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) {
                // No need to handle collapse events in this context
            }
        });

        ActionListener goActionListener = e -> changeDirectory(currentPathField.getText());
        goButton.addActionListener(goActionListener);
        currentPathField.addActionListener(goActionListener);

        showHiddenCheckBox.addItemListener(e -> {
            boolean showHidden = e.getStateChange() == ItemEvent.SELECTED;
            refreshFileList(showHidden);
        });
    }

    public void setDirectory(File directory) {
        currentFile = directory;
        currentPathField.setText(directory.getAbsolutePath());
        refreshFileList(showHiddenCheckBox.isSelected());
        backend.getSettings().setLastWorkingDirectory(directory.getAbsolutePath());
    }

    private void refreshFileList(boolean showHidden) {
        DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(new FileNode(currentFile));
        if (currentFile.getParentFile() != null) {
            newRootNode.add(new DefaultMutableTreeNode(new FileNode(null, "..")));
        }
        createChildren(newRootNode, currentFile, showHidden);

        DefaultTreeModel model = (DefaultTreeModel) fileTree.getModel();
        model.setRoot(newRootNode);
        model.reload();

        currentPathField.setText(currentFile.getAbsolutePath());
    }


    private void changeDirectory(String path) {
        File newDirectory = new File(path);
        if (newDirectory.exists() && newDirectory.isDirectory()) {
            setDirectory(newDirectory);
        } else {
            JOptionPane.showMessageDialog(this, "Directory does not exist: " + path, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createChildren(DefaultMutableTreeNode node, File file, boolean showHidden) {
        if (file == null) {
            return;
        }

        File[] files = file.listFiles();
        if (files != null) {
            for (File child : files) {
                if (!showHidden && (child.isHidden() || child.getName().startsWith(".") || child.getName().startsWith("$"))) {
                    continue;
                }
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(child));
                node.add(childNode);
                if (child.isDirectory()) {
                    childNode.add(new DefaultMutableTreeNode(new FileNode(null, "Loading...")));
                }
            }
        }
    }


    @Override
    public void UGSEvent(UGSEvent evt) {
        // React to events as needed
    }

    private static class FileNode {
        private final File file;
        private final String displayName;

        public FileNode(File file) {
            this.file = file;
            this.displayName = file.getName().isEmpty() ? file.getPath() : file.getName();
        }

        public FileNode(File file, String displayName) {
            this.file = file;
            this.displayName = displayName;
        }

        public boolean isDirectory() {
            return file.isDirectory();
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}