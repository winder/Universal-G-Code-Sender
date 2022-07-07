package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.Constants;
import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import org.openide.util.ImageUtilities;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import static com.willwinder.ugp.welcome.Constants.IMAGE_LOGO;

public class TabHeader extends JPanel implements SteppedSizeManager.SteppedSizeChangeListener {

    private final ShowNextTime showNextTime = new ShowNextTime();
    private final JPanel appLogo;

    public TabHeader(TabButton... buttons) {
        super();
        setOpaque(true);
        setLayout(new GridBagLayout());
        setBackground(Constants.COLOR_HEADER_BACKGROUND);

        appLogo = new JPanel(new BorderLayout());
        appLogo.add(new JLabel(new ImageIcon(ImageUtilities.loadImage(IMAGE_LOGO, true))));
        appLogo.setOpaque(false);

        JPanel panelButtons = new JPanel(new GridLayout(1, 0));
        panelButtons.setOpaque(false);
        for (int i = 0; i < buttons.length; i++) {
            TabButton btn = buttons[i];
            btn.setBorder(new TabBorder(i == buttons.length - 1));
            panelButtons.add(btn);
        }

        add(appLogo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 20, 8, 20), 0, 0));
        add(new JLabel(), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(panelButtons, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        add(new JLabel(), new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        add(showNextTime, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(15, 12, 15, 12), 0, 0));

        SteppedSizeManager steppedSizeManager = new SteppedSizeManager(this, new Dimension(680, 0));
        steppedSizeManager.addListener(this);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(showNextTime::requestFocusInWindow);
    }

    @Override
    public void onSizeChange(int size) {
        // Hide the logo if the size is sparse
        appLogo.setVisible(size != 0);
    }
}
