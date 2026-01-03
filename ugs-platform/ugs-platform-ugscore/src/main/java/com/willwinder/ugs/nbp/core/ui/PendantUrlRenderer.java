package com.willwinder.ugs.nbp.core.ui;

import com.willwinder.universalgcodesender.pendantui.PendantURLBean;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import java.awt.Component;

public class PendantUrlRenderer extends JLabel implements ListCellRenderer<PendantURLBean> {

    public PendantUrlRenderer() {
        setOpaque(true);
        setVerticalAlignment(SwingConstants.TOP);
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends PendantURLBean> list,
            PendantURLBean value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (value == null) {
            setText("");
            return this;
        }

        // Two-line HTML layout
        String html = "<html>"
                + "<b>" + value.getDisplayName() + "</b><br>"
                + "<span>" + value.getUrlString() + "</span>"
                + "</html>";

        setText(html);

        // Standard selection colors
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        return this;
    }
}