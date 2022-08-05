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

import com.willwinder.universalgcodesender.File;
import com.willwinder.universalgcodesender.IFileService;
import com.willwinder.universalgcodesender.uielements.helpers.LoaderDialogHelper;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.io.IOUtils;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class FileBrowserDialog extends JDialog implements MouseListener, ListSelectionListener {
    private final Logger LOGGER = Logger.getLogger(FileBrowserDialog.class.getSimpleName());

    private final FileTableModel tableModel;
    private final IFileService fileService;
    private final JTable fileTable;
    private final JButton uploadButton;
    private final JButton deleteButton;
    private final JButton downloadButton;

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
        fileTable.addMouseListener(this);

        add(new JScrollPane(fileTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteButton = new JButton("Delete", ImageUtilities.loadImageIcon("icons/delete.svg", false));
        deleteButton.addActionListener(e -> handleFileDelete());
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);

        downloadButton = new JButton("Download", ImageUtilities.loadImageIcon("icons/download.svg", false));
        downloadButton.addActionListener(e -> handleFileDownload());
        downloadButton.setEnabled(false);
        buttonPanel.add(downloadButton);

        uploadButton = new JButton("Upload", ImageUtilities.loadImageIcon("icons/upload.svg", false));
        uploadButton.addActionListener(e -> handleFileUpload());
        buttonPanel.add(uploadButton);

        add(buttonPanel, BorderLayout.SOUTH);
        refreshFileList();
        setResizable(true);
        pack();
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
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Upload file");
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setAcceptAllFileFilterUsed(true);

        int status = fileDialog.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            ThreadHelper.invokeLater(() -> {
                setEnabled(false);
                LoaderDialogHelper.showDialog("Uploading file", 1500, this);
                try {
                    byte[] buffer = IOUtils.toByteArray(new FileInputStream(fileDialog.getSelectedFile()));
                    fileService.uploadFile(fileDialog.getSelectedFile().getName(), buffer);
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
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() > 1) {
            handleFileDownload();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

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
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setDialogTitle("Download as...");
        fileDialog.setSelectedFile(new java.io.File(fileDialog.getCurrentDirectory().getAbsolutePath() + java.io.File.separatorChar + file.getName()));
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int status = fileDialog.showSaveDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            ThreadHelper.invokeLater(() -> {
                setEnabled(false);
                LoaderDialogHelper.showDialog("Downloading file", 1500, this);
                try {
                    byte[] bytes = fileService.downloadFile(file);
                    IOUtils.write(bytes, new FileOutputStream(fileDialog.getSelectedFile()));
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
        ThreadHelper.invokeLater(() -> {
            setEnabled(false);
            LoaderDialogHelper.showDialog("Deleting file", 1500, this);
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
