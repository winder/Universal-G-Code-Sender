package com.willwinder.ugs.nbp.designer.gui;

import javax.swing.*;
import java.awt.*;

public class PanelButton extends JButton {

    public static final float TITLE_SCALE = 0.8f;
    public static final String TITLE_FOREGROUND_COLOR_KEY = "Label.disabledForeground";
    private final JLabel textLabel;

    public PanelButton(String title, String text) {
        super();
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(100, 100));
        JLabel titleLabel = new JLabel(title);

        Color color = UIManager.getDefaults().getColor(TITLE_FOREGROUND_COLOR_KEY);
        titleLabel.setForeground(color);
        Font font = titleLabel.getFont();
        font = font.deriveFont(font.getSize() * TITLE_SCALE);
        titleLabel.setFont(font);

        add(titleLabel, BorderLayout.NORTH);
        this.textLabel = new JLabel(text);
        add(this.textLabel, BorderLayout.CENTER);
    }

    @Override
    public void setText(String text) {
        this.textLabel.setText(text);
    }
}
