package com.willwinder.universalgcodesender.uielements.components;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class SeparatorLabel extends JPanel {

    private final JLabel label;

    /**
     * Creates a new instance of SeparatorLabel
     *
     * @param text                the text to show
     * @param horizontalAlignment One of the following constants
     *                            defined in <code>SwingConstants</code>:
     *                            <code>LEFT</code>,
     *                            <code>CENTER</code>,
     *                            <code>RIGHT</code>,
     *                            <code>LEADING</code> or
     *                            <code>TRAILING</code>.
     */
    public SeparatorLabel(String text, int horizontalAlignment) {
        setLayout(new MigLayout("insets 0, fillx"));
        label = new JLabel(text, horizontalAlignment);
        add(label, "spanx, growx, gapbottom 0, wrap");
        add(new JSeparator(), "spanx, growx, gaptop 5, wrap");
    }

    public void setText(String text) {
        label.setText(text);
    }
}
