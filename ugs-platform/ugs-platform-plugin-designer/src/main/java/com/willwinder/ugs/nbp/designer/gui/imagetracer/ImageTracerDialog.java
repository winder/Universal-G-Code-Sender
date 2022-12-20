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
package com.willwinder.ugs.nbp.designer.gui.imagetracer;

import com.willwinder.ugs.nbp.designer.Throttler;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class ImageTracerDialog extends JDialog {
    private transient List<Entity> entities = new ArrayList<>();
    private final transient Throttler refreshThrottler;
    private final SVGCanvas svgCanvas = new SVGCanvas();
    private final TraceSettingsPanel settingsPanel = new TraceSettingsPanel();

    private File selectedFile;
    private String generatedSvgData;

    public ImageTracerDialog() {
        super((JFrame) null, true);
        setTitle("Trace image");
        setPreferredSize(new Dimension(700, 600));
        setMinimumSize(new Dimension(500, 500));
        setLayout(new MigLayout("fill, insets 5", "[170px][grow]", "[grow][20px]"));

        svgCanvas.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(settingsPanel, "grow");
        add(svgCanvas, "grow, wrap");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton openImage = new JButton("Open");
        openImage.addActionListener(e -> openFile());
        buttonPanel.add(openImage);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> ThreadHelper.invokeLater(this::generateEntities));
        buttonPanel.add(okButton);
        add(buttonPanel, "spanx, grow");

        refreshThrottler = new Throttler(this::refreshSvg, 1000);
        settingsPanel.addListener(e -> refreshThrottler.run());
        setResizable(true);
        pack();
    }

    private void refreshSvg() {
        ThreadHelper.invokeLater(() -> {
            if (selectedFile != null) {
                generatedSvgData = TraceUtils.traceImage(selectedFile, settingsPanel.getSettings());
                svgCanvas.setSvgData(generatedSvgData);
            } else {
                svgCanvas.setSVGDocument(null);
            }
        });
    }

    private void openFile() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("PNG", "png"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("JPEG", "jpeg", "jpg"));
        fileDialog.showOpenDialog(this);
        setSelectedFile(fileDialog.getSelectedFile());
    }

    private void generateEntities() {
        SvgReader svgReader = new SvgReader();
        Optional<Design> designOptional = svgReader.read(new ByteArrayInputStream(generatedSvgData.getBytes(Charset.defaultCharset())));
        designOptional.ifPresent(design -> {
            Map<String, List<Entity>> entityLayersMap = new HashMap<>();
            ((Group) design.getEntities().get(0)).getAllChildren().forEach(entity -> {
                String layerId = StringUtils.substringBetween(entity.getDescription(), "l ", " ");
                List<Entity> layerEntities = entityLayersMap.getOrDefault(layerId, new ArrayList<>());
                layerEntities.add(entity);
                entityLayersMap.put(layerId, layerEntities);
            });

            List<String> layerIds = new ArrayList<>(entityLayersMap.keySet());
            Collections.reverse(layerIds);

            entities = new ArrayList<>();
            layerIds.forEach(layerId -> {
                Group layerGroup = new Group();
                layerGroup.setName(layerId);
                layerGroup.addAll(entityLayersMap.get(layerId));
                entities.add(layerGroup);
            });
            dispose();
        });
    }


    private void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
        refreshThrottler.run();
    }

    public static void main(String[] args) {
        ImageTracerDialog insertShapeDialog = new ImageTracerDialog();
        insertShapeDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        insertShapeDialog.showDialog();
    }

    public List<Entity> showDialog() {
        setVisible(true);
        dispose();
        return entities;
    }
}
