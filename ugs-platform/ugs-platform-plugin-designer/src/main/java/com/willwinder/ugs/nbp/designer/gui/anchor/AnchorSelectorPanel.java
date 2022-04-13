package com.willwinder.ugs.nbp.designer.gui.anchor;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import static com.willwinder.ugs.nbp.designer.entities.Anchor.*;


public class AnchorSelectorPanel extends JPanel implements ActionListener {
    private final JRadioButton topLeft;
    private final JRadioButton topRight;
    private final JRadioButton center;
    private final JRadioButton bottomRight;
    private final JRadioButton bottomLeft;
    private final Set<AnchorListener> anchorListeners = new HashSet<>();

    public AnchorSelectorPanel() {
        setLayout(new MigLayout("fill, insets 0, gap 2"));

        topLeft = new JRadioButton();
        topLeft.setBorder(BorderFactory.createEmptyBorder());
        add(topLeft, "alignx center, growy");

        topRight = new JRadioButton();
        topRight.setBorder(BorderFactory.createEmptyBorder());
        add(topRight, "alignx center, growy, wrap");

        center = new JRadioButton();
        center.setBorder(BorderFactory.createEmptyBorder());
        add(center, "alignx center, spanx, growy, wrap");

        bottomLeft = new JRadioButton();
        bottomLeft.setSelected(true);
        bottomLeft.setBorder(BorderFactory.createEmptyBorder());
        add(bottomLeft, "alignx center, growy");

        bottomRight = new JRadioButton();
        bottomRight.setBorder(BorderFactory.createEmptyBorder());
        add(bottomRight, "alignx center, growy, wrap");

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(topLeft);
        buttonGroup.add(topRight);
        buttonGroup.add(center);
        buttonGroup.add(bottomLeft);
        buttonGroup.add(bottomRight);

        topLeft.addActionListener(this);
        topRight.addActionListener(this);
        center.addActionListener(this);
        bottomLeft.addActionListener(this);
        bottomRight.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JRadioButton button = (JRadioButton) e.getSource();
        if (button == topLeft) {
            notifySelectedAnchor(TOP_LEFT);
        } else if (button == topRight) {
            notifySelectedAnchor(TOP_RIGHT);
        } else if (button == center) {
            notifySelectedAnchor(CENTER);
        } else if (button == bottomLeft) {
            notifySelectedAnchor(BOTTOM_LEFT);
        } else if (button == bottomRight) {
            notifySelectedAnchor(BOTTOM_RIGHT);
        }
    }

    public void addListener(AnchorListener anchorListener) {
        anchorListeners.add(anchorListener);
    }

    private void notifySelectedAnchor(Anchor anchor) {
        anchorListeners.forEach(l -> l.onAnchorChanged(anchor));
    }

    @Override
    public void setEnabled(boolean enabled) {
        topLeft.setEnabled(enabled);
        topRight.setEnabled(enabled);
        center.setEnabled(enabled);
        bottomLeft.setEnabled(enabled);
        bottomRight.setEnabled(enabled);
    }

    public void setAnchor(Anchor anchor) {
        if (anchor == TOP_LEFT) {
            topLeft.setSelected(true);
        } else if (anchor == TOP_RIGHT) {
            topRight.setSelected(true);
        } else if (anchor == CENTER) {
            center.setSelected(true);
        } else if (anchor == BOTTOM_LEFT) {
            bottomLeft.setSelected(true);
        } else if (anchor == BOTTOM_RIGHT) {
            bottomRight.setSelected(true);
        }
    }
}
