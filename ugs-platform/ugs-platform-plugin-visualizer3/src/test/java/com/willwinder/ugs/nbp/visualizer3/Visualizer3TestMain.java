package com.willwinder.ugs.nbp.visualizer3;

import com.willwinder.ugs.nbp.visualizer3.actions.OpenTestFileAction;
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

public class Visualizer3TestMain extends JFrame {

    public static void main(String[] args) throws Exception {
        Visualizer3TestMain visualizerMain = new Visualizer3TestMain();
        visualizerMain.start();

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Visualizer test");
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private void start() throws Exception {
        setPreferredSize(new Dimension(1024, 768));
        setLayout(new BorderLayout());

        Visualizer3TopComponent visualizer = new Visualizer3TopComponent();
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
