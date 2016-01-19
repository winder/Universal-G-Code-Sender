package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.utils.Settings;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MacroTextField extends JTextField {

    private final Integer index;
    private final Settings settings;

    public MacroTextField(final Integer index, Settings settings) {
        this.index = index;
        this.settings = settings;

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                //ignore
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //ignore
            }

            @Override
            public void keyReleased(KeyEvent e) {
                MacroTextField.this.settings.updateMacro(MacroTextField.this.index, MacroTextField.this.getText());
            }
        });
    }
}
