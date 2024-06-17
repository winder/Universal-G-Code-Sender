package com.willwinder.ugs.nbp.designer.gui.anchor;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.BOTTOM_CENTER;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.BOTTOM_LEFT;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.BOTTOM_RIGHT;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.CENTER;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.LEFT_CENTER;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.RIGHT_CENTER;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.TOP_CENTER;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.TOP_LEFT;
import static com.willwinder.ugs.nbp.designer.entities.Anchor.TOP_RIGHT;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;


public class AnchorSelectorPanel extends JPanel implements ActionListener {
    public static final String FIELD_CONSTRAINTS = "alignx center, growy";
    public static final String FIELD_CONSTRAINTS_WRAP = "alignx center, growy, wrap";
    private final JRadioButton topLeft;
    private final JRadioButton topCenter;
    private final JRadioButton topRight;
    private final JRadioButton center;
    private final JRadioButton bottomRight;
    private final JRadioButton bottomCenter;
    private final JRadioButton bottomLeft;
    private final JRadioButton leftCenter;
    private final JRadioButton rightCenter;
    private final Set<AnchorListener> anchorListeners = new HashSet<>();

    public AnchorSelectorPanel() {
        setLayout(new MigLayout("fill, insets 0, gap 0"));

        topLeft = new JRadioButton();
        topLeft.setBorder(BorderFactory.createEmptyBorder());
        add(topLeft, FIELD_CONSTRAINTS);

        topCenter = new JRadioButton();
        topCenter.setBorder(BorderFactory.createEmptyBorder());
        add(topCenter, FIELD_CONSTRAINTS);

        topRight = new JRadioButton();
        topRight.setBorder(BorderFactory.createEmptyBorder());
        add(topRight, FIELD_CONSTRAINTS_WRAP);

        leftCenter = new JRadioButton();
        leftCenter.setBorder(BorderFactory.createEmptyBorder());
        add(leftCenter, FIELD_CONSTRAINTS);

        center = new JRadioButton();
        center.setBorder(BorderFactory.createEmptyBorder());
        add(center, FIELD_CONSTRAINTS);

        rightCenter = new JRadioButton();
        rightCenter.setBorder(BorderFactory.createEmptyBorder());
        add(rightCenter, FIELD_CONSTRAINTS_WRAP);

        bottomLeft = new JRadioButton();
        bottomLeft.setSelected(true);
        bottomLeft.setBorder(BorderFactory.createEmptyBorder());
        add(bottomLeft, FIELD_CONSTRAINTS);

        bottomCenter = new JRadioButton();
        bottomCenter.setBorder(BorderFactory.createEmptyBorder());
        add(bottomCenter, FIELD_CONSTRAINTS);

        bottomRight = new JRadioButton();
        bottomRight.setBorder(BorderFactory.createEmptyBorder());
        add(bottomRight, FIELD_CONSTRAINTS_WRAP);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(topLeft);
        buttonGroup.add(topCenter);
        buttonGroup.add(topRight);
        buttonGroup.add(leftCenter);
        buttonGroup.add(center);
        buttonGroup.add(rightCenter);
        buttonGroup.add(bottomLeft);
        buttonGroup.add(bottomCenter);
        buttonGroup.add(bottomRight);

        topLeft.addActionListener(this);
        topCenter.addActionListener(this);
        topRight.addActionListener(this);
        leftCenter.addActionListener(this);
        center.addActionListener(this);
        rightCenter.addActionListener(this);
        bottomLeft.addActionListener(this);
        bottomCenter.addActionListener(this);
        bottomRight.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JRadioButton button = (JRadioButton) e.getSource();
        if (button == topLeft) {
            notifySelectedAnchor(TOP_LEFT);
        } else if (button == topCenter) {
            notifySelectedAnchor(TOP_CENTER);
        } else if (button == topRight) {
            notifySelectedAnchor(TOP_RIGHT);
        } else if (button == leftCenter) {
            notifySelectedAnchor(LEFT_CENTER);
        } else if (button == center) {
            notifySelectedAnchor(CENTER);
        } else if (button == rightCenter) {
            notifySelectedAnchor(RIGHT_CENTER);
        } else if (button == bottomLeft) {
            notifySelectedAnchor(BOTTOM_LEFT);
        } else if (button == bottomCenter) {
            notifySelectedAnchor(BOTTOM_CENTER);
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
        topCenter.setEnabled(enabled);
        topRight.setEnabled(enabled);
        leftCenter.setEnabled(enabled);
        center.setEnabled(enabled);
        rightCenter.setEnabled(enabled);
        bottomLeft.setEnabled(enabled);
        bottomCenter.setEnabled(enabled);
        bottomRight.setEnabled(enabled);
    }

    @Override
    public void setVisible(boolean visible) {
        topLeft.setVisible(visible);
        topCenter.setVisible(visible);
        topRight.setVisible(visible);
        leftCenter.setVisible(visible);
        center.setVisible(visible);
        rightCenter.setVisible(visible);
        bottomLeft.setVisible(visible);
        bottomCenter.setVisible(visible);
        bottomRight.setVisible(visible);
    }

    public void setAnchor(Anchor anchor) {
        if (anchor == TOP_LEFT) {
            topLeft.setSelected(true);
        } else if (anchor == TOP_CENTER) {
            topCenter.setSelected(true);
        } else if (anchor == TOP_RIGHT) {
            topRight.setSelected(true);
        } else if (anchor == LEFT_CENTER) {
            leftCenter.setSelected(true);
        } else if (anchor == CENTER) {
            center.setSelected(true);
        } else if (anchor == RIGHT_CENTER) {
            rightCenter.setSelected(true);
        } else if (anchor == BOTTOM_LEFT) {
            bottomLeft.setSelected(true);
        } else if (anchor == BOTTOM_RIGHT) {
            bottomRight.setSelected(true);
        } else if (anchor == BOTTOM_CENTER) {
            bottomCenter.setSelected(true);
        }
    }
}
