/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.core.panels;

import com.willwinder.universalgcodesender.pendantui.PendantURLBean;
import org.openide.util.ImageUtilities;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * A panel that will show the QR code for accessing the web pendant
 *
 * @author Joacim Breiler
 */
public class QRPanel extends JPanel {
    private final ImageIcon imageIcon;
    private final ImageIcon qrIcon;

    public QRPanel(PendantURLBean pendantURLBean) {
        this.imageIcon = ImageUtilities.loadImageIcon("/resources/images/cell-phone.svg", false);
        this.qrIcon = new ImageIcon(pendantURLBean.getQrCodeJpg(), "QR Code");
        setBorder(new EmptyBorder(0,0,0,0));
        setMinimumSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
        setPreferredSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
        setMaximumSize(new Dimension(imageIcon.getIconWidth(), imageIcon.getIconHeight()));
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(imageIcon.getImage(), 0,0, null);

        int x = (imageIcon.getIconWidth() - qrIcon.getIconWidth()) / 2;
        g2.drawImage(qrIcon.getImage(), x, 120, null);
    }
}
