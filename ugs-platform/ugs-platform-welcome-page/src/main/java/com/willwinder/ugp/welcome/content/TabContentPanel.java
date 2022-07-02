package com.willwinder.ugp.welcome.content;

import com.willwinder.ugp.welcome.Constants;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

public class TabContentPanel extends JPanel {
    public TabContentPanel(List<JComponent> tabs) {
        super(new MigLayout("fill, insets 0, hidemode 3, wrap 1"));
        setOpaque(false);

        for (JComponent c : tabs) {
            add(c, "grow");
            c.setVisible(false);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (null != getParent() && null != getParent().getParent()) {
            Component scroll = getParent().getParent();
            if (scroll.getWidth() > 0) {
                if (d.width > scroll.getWidth()) {
                    d.width = Math.max(scroll.getWidth(), Constants.START_PAGE_MIN_WIDTH);
                } else if (d.width < scroll.getWidth()) {
                    d.width = scroll.getWidth();
                }
            }
        }
        d.width = Math.min(d.width, Constants.START_PAGE_MIN_WIDTH);
        return d;
    }
}
