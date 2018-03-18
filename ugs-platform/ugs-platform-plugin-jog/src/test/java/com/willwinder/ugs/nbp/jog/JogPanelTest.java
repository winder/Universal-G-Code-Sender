package com.willwinder.ugs.nbp.jog;

import com.willwinder.ugs.nbp.jog.actions.UseSeparateStepSizeAction;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import org.mockito.Mockito;

import javax.swing.*;

import static org.mockito.Mockito.when;

public class JogPanelTest extends JFrame {
    private JogPanel jogPanel;

    public static void main(String[] args) throws Exception {
        JogPanelTest jogPanelTest = new JogPanelTest();
        jogPanelTest.start();
    }

    private void start() throws Exception {
        JogService jogService = Mockito.mock(JogService.class);
        when(jogService.getUnits()).thenReturn(UnitUtils.Units.INCH);
        jogPanel = new JogPanel(jogService, new UseSeparateStepSizeAction().getMenuPresenter());
        getContentPane().add(jogPanel);

        createMenuBar();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setVisible(true);

        jogPanel.setFeedRate(1000);
        jogPanel.setStepSizeXY(100);
        jogPanel.setStepSizeZ(0.01);

        setMinimumSize(jogPanel.getMinimumSize());
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

        menuItem = new JMenuItem("Use Z step size");
        menuItem.addActionListener(e -> jogPanel.setUseStepSizeZ(true));
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Don't use Z step size");
        menuItem.addActionListener(e -> jogPanel.setUseStepSizeZ(false));
        fileMenu.add(menuItem);

        setJMenuBar(menuBar);
    }
}
