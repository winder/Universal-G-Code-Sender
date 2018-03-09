package com.willwinder.ugs.nbp.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeListener;

public class JogPanelTest extends JFrame {
    private JogPanel jogPanel;

    public static void main(String[] args) throws Exception {
        JogPanelTest jogPanelTest = new JogPanelTest();
        jogPanelTest.start();
    }

    private void start() throws Exception {
        setMinimumSize(new Dimension(100, 100));
        setPreferredSize(new Dimension(250, 300));

        jogPanel = new JogPanel();
        getContentPane().add(jogPanel);

        createMenuBar();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setVisible(true);

        jogPanel.setJogFeedRate(1000);
        jogPanel.setXyStepLength(100);
        jogPanel.setZStepLength(0.01);
    }

    private void createMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem("Enabled");
        menuItem.addActionListener(e -> jogPanel.setEnabled(true));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Disabled");
        menuItem.addActionListener(e -> jogPanel.setEnabled(false));
        fileMenu.add(menuItem);

        setJMenuBar(menuBar);
    }
}
