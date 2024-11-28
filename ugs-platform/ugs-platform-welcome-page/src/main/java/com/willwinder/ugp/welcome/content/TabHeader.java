package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.Constants;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GridLayout;

public class TabHeader extends JPanel implements SteppedSizeManager.SteppedSizeChangeListener {

    private final ShowNextTime showNextTime = new ShowNextTime();

    public TabHeader(TabButton... buttons) {
        super();
        setOpaque(true);
        setLayout(new MigLayout("fill, insets 0, height 50"));
        setBackground(Constants.COLOR_HEADER_BACKGROUND);

        JPanel panelButtons = new JPanel(new GridLayout(1, 0));
        panelButtons.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            TabButton btn = buttons[i];
            btn.setBorder(new TabBorder(i == buttons.length - 1));
            panelButtons.add(btn);
        }

        add(panelButtons, "growy, align left");
        add(showNextTime, "align right, gapright 8");

        SteppedSizeManager steppedSizeManager = new SteppedSizeManager(this, new Dimension(720, 0));
        steppedSizeManager.addListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(showNextTime::requestFocusInWindow);
    }

    @Override
    public void onSizeChange(int size) {
    }
}
