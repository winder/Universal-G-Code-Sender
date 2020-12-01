/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.jog;

import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * A button that will automatically will disable the icon if the button size is too small
 *
 * @author Joacim Breiler
 */
public class JogButton extends JButton implements SteppedSizeManager.SteppedSizeChangeListener {

    private final ImageIcon imageIcon;

    public JogButton(JogPanelButtonEnum buttonEnum) {
        setText(buttonEnum.getText());
        setVerticalTextPosition(buttonEnum.getVerticalAligment());
        setHorizontalTextPosition(buttonEnum.getHorisontalAlignment());

        imageIcon = ImageUtilities.loadImageIcon(buttonEnum.getIconUrl(), false);
        setIcon(imageIcon);

        setMargin(new Insets(0,0,0,0));
        setFocusable(false);

        SteppedSizeManager steppedSizeManager = new SteppedSizeManager(this, new Dimension(40, 40));
        steppedSizeManager.addListener(this);
    }

    @Override
    public void onSizeChange(int size) {
        if(size == 0 && getText().length() > 0) {
            setIcon(null);
        } else {
            setIcon(imageIcon);
        }
    }
}
