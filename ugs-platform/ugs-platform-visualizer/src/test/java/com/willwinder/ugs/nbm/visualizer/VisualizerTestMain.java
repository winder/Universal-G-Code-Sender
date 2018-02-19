package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.actions.OpenGcodeFileAction;

import javax.swing.*;
import java.awt.*;

/**
 * A class for viewing and testing a stand alone version of the visualizer
 *
 * @author Joacim Breiler
 */
public class VisualizerTestMain extends JFrame {

    public static void main(String[] args) throws Exception {
        VisualizerTestMain visualizerMain = new VisualizerTestMain();
        visualizerMain.start();
    }

    private void start() throws Exception {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);

        setPreferredSize(new Dimension(1024, 768));

        Visualizer2TopComponent visualizer = new Visualizer2TopComponent();
        getContentPane().add(visualizer);
        visualizer.componentOpened();

        createMenuBar(backendAPI);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setVisible(true);
    }

    private void createMenuBar(BackendAPI backendAPI) {
        JMenu fileMenu = new JMenu("File");
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem(new OpenGcodeFileAction(backendAPI));
        menuItem.setText("Open");
        fileMenu.add(menuItem);
        setJMenuBar(menuBar);
    }
}
