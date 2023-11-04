package com.willwinder.universalgcodesender.uielements.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Register this as a listener to a text field and it will show a placeholder
 */
public class TextFieldPlaceholderFocusListener implements FocusListener {

    private final JTextField textField;
    private final Color textColor;
    private final Color placeholderColor;
    private final String placeholderText;

    public TextFieldPlaceholderFocusListener(JTextField textArea, String placeholderText) {
        this.textField = textArea;
        textColor = textArea.getForeground();
        placeholderColor = textArea.getDisabledTextColor();
        this.placeholderText = placeholderText;
        focusLost(null);
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (textField.getText().equals(placeholderText)) {
            textField.setText("");
            textField.setForeground(textColor);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (textField.getText().isEmpty()) {
            textField.setForeground(placeholderColor);
            textField.setText(placeholderText);
        }
    }
}
