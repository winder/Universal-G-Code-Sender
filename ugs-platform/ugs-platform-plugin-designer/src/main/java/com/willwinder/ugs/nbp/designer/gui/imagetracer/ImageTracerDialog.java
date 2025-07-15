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
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class ImageTracerDialog extends JDialog {
    private final static String DEFAULT_SVG = "<svg height='30' width='200' xmlns='http://www.w3.org/2000/svg'>\n<text x='5' y='15' fill='black'>No File Selected</text>\n</svg>";
    public static final double FLATNESS_PRECISION = 0.1d;

    private transient List<Entity> entities = new ArrayList<>();
    private final transient Throttler refreshThrottler;
    private final SVGCanvas svgCanvas = new SVGCanvas();
    private final TraceSettingsPanel settingsPanel = new TraceSettingsPanel();

    private File selectedFile;
    private String generatedSvgData;
    private final JTabbedPane tabs = new JTabbedPane();
    
    public ImageTracerDialog() {
        super((JFrame) null, true);
        setTitle("Import Depth Map");
        setPreferredSize(new Dimension(1024, 768));
        setMinimumSize(new Dimension(500, 500));
        setLayout(new MigLayout("fill, insets 5", "[170px][grow]", "[grow][20px]"));

        svgCanvas.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(settingsPanel, "grow");
        tabs.addTab("Preview", svgCanvas);
        add(tabs, "grow, wrap");

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
        refreshThrottler.run();
    }

    private void refreshSvg() {
        ThreadHelper.invokeLater(() -> {
            if (selectedFile != null) {
                generatedSvgData = TraceUtils.traceImage(selectedFile, settingsPanel.getSettings());
                svgCanvas.setSvgData(generatedSvgData);
            } else {
                svgCanvas.setSvgData(DEFAULT_SVG);
//                svgCanvas.setSVGDocument(null);
            }
        });
    }
    private String lastOpenedFile = null;
            
    private void openFile() {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setAcceptAllFileFilterUsed(false);
        if (lastOpenedFile != null) {
            fileDialog.setSelectedFile(new File(lastOpenedFile));
        }
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("All Supported Image Types", "png","pnm","jpeg","jpg","tiff","tif","bmp","wbmp","gif"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("PNG", "png","pnm"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("JPEG", "jpeg", "jpg"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("TIFF", "tiff", "tif"));        
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("BITMAP", "bmp","wbmp"));                
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("GIF", "gif"));        
        
        if (fileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {        
            lastOpenedFile = fileDialog.getSelectedFile().getAbsolutePath(); 
            setSelectedFile(fileDialog.getSelectedFile());
        }
    }
    private double calcAreaSize(Area area){
        double sum = 0;

        double xBegin=0, yBegin=0, xPrev=0, yPrev=0, coords[] = new double[6];
        for (PathIterator iterator1 = area.getPathIterator(null, 0.1); !iterator1.isDone(); iterator1.next()){
            switch (iterator1.currentSegment(coords))
            {
                case PathIterator.SEG_MOVETO:
                    xBegin = coords[0]; yBegin = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    // the well-known trapez-formula
                    sum += (coords[0] - xPrev) * (coords[1] + yPrev) / 2.0;
                    break;
                case PathIterator.SEG_CLOSE:
                    sum += (xBegin - xPrev) * (yBegin + yPrev) / 2.0;
                    break;
                default:
                    // curved segments cannot occur, because we have a flattened ath
                    throw new InternalError();
            }
            xPrev = coords[0]; yPrev = coords[1];
        }
        return sum;
    }

    private void generateEntities() {
                
        TraceSettings settings = settingsPanel.getSettings();
        boolean doAdvancedAdd = settings.isEnableAdvancedMode();
        boolean shouldCutContents = settings.isCutLayerContents();
        
        double minumumItemSize = settings.getMinimumDetailSize(); // Speed optimization. 
        Rectangle2D.Double outputBounds = new Rectangle2D.Double(0,0,1,1);
        
        SvgReader svgReader = new SvgReader();
        Optional<Design> designOptional = svgReader.read(new ByteArrayInputStream(generatedSvgData.getBytes(Charset.defaultCharset())));
        designOptional.ifPresent(design -> {
            
            
            Map<String, List<Entity>> entityLayersMap = new HashMap<>();
            ((Group) design.getEntities().get(0)).getAllChildren().forEach(entity -> {
                String layerId = StringUtils.substringBetween(entity.getDescription(), "l ", " ");
                List<Entity> layerEntities = entityLayersMap.getOrDefault(layerId, new ArrayList<>());
                layerEntities.add(entity);
                Rectangle2D tmpBounds = entity.getBounds();
                outputBounds.x = Math.min(outputBounds.getX(), tmpBounds.getX());
                outputBounds.y = Math.min(outputBounds.getY(), tmpBounds.getY());
                outputBounds.width = Math.max(outputBounds.getWidth(), tmpBounds.getX()+tmpBounds.getWidth());
                outputBounds.height = Math.max(outputBounds.getHeight(), tmpBounds.getY()+tmpBounds.getHeight());
                
                entityLayersMap.put(layerId, layerEntities);
            });
            
            ProgressMonitor pm = new ProgressMonitor(this, "Generating Slices", "Note", 0, entityLayersMap.keySet().size());
            
            List<String> layerIds = new ArrayList<>();//entityLayersMap.keySet());
            for (int x = 0; x < entityLayersMap.keySet().size(); x++) {
                layerIds.add("" + x);
            } 
            entities = new ArrayList<>();           
            double layerCount = layerIds.size();
            double stepSize = (settings.getTargetDepth() - settings.getStartDepth()) / layerCount;             
            int progressCounter = 0;
            try {
                for (String layerId : layerIds) {            

                    Group layerGroup = new Group();
                    pm.setNote ("Slice : " + progressCounter + " / " + layerCount);
                    var lIndex = Integer.parseInt(layerId);
                    double targetPos = settings.getStartDepth() + (stepSize * ( lIndex + 1 ) );
                    double startPos = settings.getStartDepth() + (stepSize * lIndex);
                    int curLayerIndex = Integer.parseInt(layerId);
                    if (doAdvancedAdd) {
                        pm.setMillisToDecideToPopup(0);
                        layerGroup.setName("Index: " + layerId  + " Start Depth: "+ settings.getStartDepth() + " Start Depth: " + startPos + " Target Depth: " + targetPos);

                        for (int x = 0 ; x <= curLayerIndex; x++) {                            
                            List<Entity> tmpEntity = entityLayersMap.get(""+x);
                            for (Entity entity : tmpEntity) {
                                double eArea = entity.getBounds().getWidth() * entity.getBounds().getWidth();
                                if (eArea >= minumumItemSize) {
                                    layerGroup.addChild(entity);                                
                                } 
                            }
                            pm.setProgress(progressCounter);

                        }
                        /////////////
                        Area sliceArea = new Area(outputBounds.getBounds2D());

                        
                        layerGroup.getAllChildren().forEach(groupEntity -> sliceArea.subtract(new Area(groupEntity.getShape())));                    
                        pm.setProgress(progressCounter);

                        Path path = new Path();                    
                        path.setCutType(CutType.POCKET);
                        path.setStartDepth(startPos);
                        path.setTargetDepth(targetPos);
                        path.setName(String.format("Index: %s Start Depth: $%.2f Target Depth: $%.2f", layerId, startPos, targetPos));
                        path.setName("Index: " + layerId  + " Start Depth: " + startPos + " Target Depth: " + targetPos);
                        path.append(cleanGarbage(sliceArea,settings.isCutLayerContents(), settings.getMinimumDetailSize()) );
                        
                        
                        pm.setNote("Made the Path");
                        entities.add(path);
                    } else {
                        layerGroup.addAll(entityLayersMap.get(layerId));  
                        entities.add(layerGroup);
                    }
                    // todo: Add feature for tool change between course tool and fine tool. 

                    pm.setProgress(progressCounter++);
                    if (pm.isCanceled()) {
                        throw new Exception("User Cancelled");
                    }
                }
                pm.setNote("Finished !!!");
                pm.close();
                dispose();
            } catch (Exception e) {
                pm.close();
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        });
    }
    private Double getShapeSize(Shape shape) {
        return calcAreaSize(new Area(shape));
    }
    
    private Area cleanGarbage(Area input, boolean shouldInvertOutput, double minSize) {
        
        Geometry geometry = ToolPathUtils.convertAreaToGeometry(input, new GeometryFactory(), FLATNESS_PRECISION);

        List<Shape> shapeList = new ArrayList<>();
        List<Shape> shapeListOut = new ArrayList<>();
        ShapeWriter shapeWriter = new ShapeWriter();

        if (geometry.getNumGeometries() > 1) { // If the shape consists of multiple geometries
            for (int i = 0; i < geometry.getNumGeometries(); i++) {
                shapeList.add(shapeWriter.toShape(geometry.getGeometryN(i)));
            }
        } else if (geometry instanceof Polygon polygon) { // If the shape consists of a polygon
            shapeList.add(shapeWriter.toShape(polygon.getExteriorRing()));
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                shapeList.add(shapeWriter.toShape(polygon.getInteriorRingN(i)));
            }
        }
        Shape targetShape = shapeList.get(0);
        double curShapeSize = getShapeSize(targetShape);
        
        for (Shape shape: shapeList) {
            double tmpShapeSize = getShapeSize(shape);
            if (tmpShapeSize >= minSize) {
                shapeListOut.add(shape);
            }
        }
        
        if (shouldInvertOutput) {
            Area toReturn = new Area(input.getBounds2D());
            for (Shape shape: shapeListOut) {
                toReturn.subtract(new Area(shape));
            }
            return toReturn;
        } else {
            Area toReturn = new Area();
            for (Shape shape: shapeListOut) {
                toReturn.add(new Area(shape));
            }                        
            return toReturn;
        }
        
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
