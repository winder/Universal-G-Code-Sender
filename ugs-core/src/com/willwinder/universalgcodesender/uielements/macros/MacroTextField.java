/*
    Copywrite 2016 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.uielements.macros;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.types.Macro;
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
        this.setToolTipText(Localization.getString("macroPanel.text"));

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                update();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                update();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                update();
            }
        });
    }

    private void update() {
        Macro macro = this.settings.getMacro(this.index);
        this.settings.updateMacro(this.index, macro.getName(), macro.getDescription(), this.getText());
    }
}
