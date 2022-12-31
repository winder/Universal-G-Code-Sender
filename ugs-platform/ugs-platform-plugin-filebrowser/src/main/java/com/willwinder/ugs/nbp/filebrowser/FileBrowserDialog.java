/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.filebrowser;

import com.willwinder.universalgcodesender.model.File;
import com.willwinder.universalgcodesender.IFileService;
import com.willwinder.universalgcodesender.uielements.components.TableCellListener;
import com.willwinder.universalgcodesender.uielements.helpers.LoaderDialogHelper;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.io.IOUtils;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class FileBrowserDialog extends JDialog implements ListSelectionListener {
    private final Logger LOGGER = Logger.getLogger(FileBrowserDialog.class.getSimpleName());

    private final FileTableModel tableModel;
    private final IFileService fileService;
    private final JTable fileTable;
    private final JButton uploadButton;
    private final JButton deleteButton;
    private final JButton downloadButton;
    private final JFileChooser fileChooser = new JFileChooser();

    public FileBrowserDialog(IFileService fileService) {
        super((JFrame) null, true);
        this.fileService = fileService;
        setTitle("File browser");
        setPreferredSize(new Dimension(300, 400));
        setMinimumSize(new Dimension(200, 300));
        setLayout(new BorderLayout());

        tableModel = new FileTableModel();
        fileTable = new FileTable(tableModel);
        fileTable.getSelectionModel().addListSelectionListener(this);
        new TableCellListener(fileTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof TableCellListener) {
                    TableCellListener tableCellListener = (TableCellListener) e.getSource();
                    String currentFilename = (String) tableCellListener.getOldValue();
                    String newFileName = (String) tableCellListener.getNewValue();
                    handleFileRename(currentFilename, newFileName);
                }
            }
        });

        add(new JScrollPane(fileTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteButton = new JButton("Delete", ImageUtilities.loadImageIcon("icons/delete.svg", false));
        deleteButton.addActionListener(e -> SwingUtilities.invokeLater(this::handleFileDelete));
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);

        downloadButton = new JButton("Download", ImageUtilities.loadImageIcon("icons/download.svg", false));
        downloadButton.addActionListener(e -> SwingUtilities.invokeLater(this::handleFileDownload));
        downloadButton.setEnabled(false);
        buttonPanel.add(downloadButton);

        uploadButton = new JButton("Upload", ImageUtilities.loadImageIcon("icons/upload.svg", false));
        uploadButton.addActionListener(e -> SwingUtilities.invokeLater(this::handleFileUpload));
        buttonPanel.add(uploadButton);

        add(buttonPanel, BorderLayout.SOUTH);
        refreshFileList();
        setResizable(true);
        pack();
    }

    private void handleFileRename(String currentFilename, String newFilename) {
        setEnabled(false);
        LoaderDialogHelper.showDialog("Renaming file", 1500, this);
        LOGGER.info("Renaming file " + currentFilename + " to " + newFilename);
        ThreadHelper.invokeLater(() -> {
            try {
                File currentFile = new File(currentFilename, currentFilename, 0);
                byte[] bytes = fileService.downloadFile(currentFile);
                Thread.sleep(2000);
                fileService.uploadFile(newFilename, bytes);
                Thread.sleep(2000);
                fileService.deleteFile(currentFile);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                LoaderDialogHelper.closeDialog();
                setEnabled(true);
                refreshFileList();
            }
        });
    }

    private void refreshFileList() {
        try {
            tableModel.replace(fileService.getFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDialog() {
        setVisible(true);
        dispose();
    }


    private void handleFileUpload() {
        fileChooser.setDialogTitle("Upload file");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(true);
        int status = fileChooser.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            setEnabled(false);
            LoaderDialogHelper.showDialog("Uploading file", 1500, this);
            ThreadHelper.invokeLater(() -> {
                try {
                    byte[] buffer = IOUtils.toByteArray(new FileInputStream(fileChooser.getSelectedFile()));
                    fileService.uploadFile(fileChooser.getSelectedFile().getName(), buffer);
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    LoaderDialogHelper.closeDialog();
                    setEnabled(true);
                }

                refreshFileList();
            });
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        fileTable.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
    }

    private void handleFileDownload() {
        File file = tableModel.get(getSelectedModelIndex());

        fileChooser.setDialogTitle("Download as...");
        fileChooser.setSelectedFile(new java.io.File(fileChooser.getCurrentDirectory().getAbsolutePath() + java.io.File.separatorChar + file.getName()));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int status = fileChooser.showSaveDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            setEnabled(false);
            LoaderDialogHelper.showDialog("Downloading file", 1500, this);
            ThreadHelper.invokeLater(() -> {
                try {
                    byte[] bytes = fileService.downloadFile(file);
                    IOUtils.write(bytes, new FileOutputStream(fileChooser.getSelectedFile()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    LoaderDialogHelper.closeDialog();
                    setEnabled(true);
                }
            });
        }
    }

    private void handleFileDelete() {
        setEnabled(false);
        LoaderDialogHelper.showDialog("Deleting file", 1500, this);

        ThreadHelper.invokeLater(() -> {
            try {
                File file = tableModel.get(getSelectedModelIndex());
                LOGGER.info("Deleting file " + file.getAbsolutePath());
                fileService.deleteFile(file);
                refreshFileList();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                LoaderDialogHelper.closeDialog();
                setEnabled(true);
            }
        });
    }

    protected int getSelectedModelIndex() {
        return fileTable.getRowSorter().convertRowIndexToModel(fileTable.getSelectedRow());
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean enabled = !fileTable.getSelectionModel().isSelectionEmpty();
        downloadButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
    }
}
