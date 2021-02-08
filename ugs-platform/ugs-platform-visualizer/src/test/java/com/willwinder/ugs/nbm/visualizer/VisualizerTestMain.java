package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.ugs.nbm.visualizer.actions.OpenTestFileAction;
import com.willwinder.ugs.nbp.core.actions.BaudRateAction;
import com.willwinder.ugs.nbp.core.actions.ConnectDisconnectAction;
import com.willwinder.ugs.nbp.core.actions.FirmwareAction;
import com.willwinder.ugs.nbp.core.actions.PortAction;
import com.willwinder.ugs.nbp.core.actions.ReturnToZeroAction;
import com.willwinder.ugs.nbp.core.actions.SoftResetAction;
import com.willwinder.ugs.nbp.core.actions.UnlockAction;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * A class for viewing and testing a stand alone version of the visualizer
 *
 * @author Joacim Breiler
 */
public class VisualizerTestMain extends JFrame {

    public static void main(String[] args) throws Exception {
        VisualizerTestMain visualizerMain = new VisualizerTestMain();
        visualizerMain.start();

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Visualizer test");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private void start() throws Exception {
        setPreferredSize(new Dimension(1024, 768));
        setLayout(new BorderLayout());

        Visualizer2TopComponent visualizer = new Visualizer2TopComponent();
        add(visualizer, BorderLayout.CENTER);
        visualizer.componentOpened();

        createMenuBar();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setVisible(true);
    }

    private void createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem(new OpenTestFileAction());
        menuItem.setText("Open");
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(new ConnectDisconnectAction());
        menuItem.setText("Connect");
        fileMenu.add(menuItem);

        menuItem = new JMenuItem(new UnlockAction());
        menuItem.setText("Unlock");
        fileMenu.add(menuItem);

        setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();
        toolBar.add(new OpenTestFileAction());
        toolBar.add(new ConnectDisconnectAction());
        toolBar.add(new FirmwareAction().getToolbarPresenter());
        toolBar.add(new PortAction().getToolbarPresenter());
        toolBar.add(new BaudRateAction().getToolbarPresenter());
        toolBar.add(new UnlockAction());
        toolBar.add(new SoftResetAction());
        toolBar.add(new ReturnToZeroAction());
        add(toolBar, BorderLayout.NORTH);

    }
}
