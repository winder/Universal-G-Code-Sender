package com.willwinder.ugs.nbp.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;

import javax.swing.*;
import java.awt.*;

public class JogPanelTest extends JFrame {
    public static void main(String[] args) throws Exception {
        JogPanelTest jogPanelTest = new JogPanelTest();
        jogPanelTest.start();
    }

    private void start() throws Exception {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);

        setPreferredSize(new Dimension(1024, 768));

        JogPanel jogPanel = new JogPanel();
        getContentPane().add(jogPanel);

        //createMenuBar(backendAPI);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        pack();
        setVisible(true);
    }

    private void createMenuBar(BackendAPI backendAPI) {
        JMenu fileMenu = new JMenu("File");
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(fileMenu);

        JMenuItem menuItem = new JMenuItem();
        menuItem.setText("Connected");
        fileMenu.add(menuItem);
        setJMenuBar(menuBar);
    }
}
