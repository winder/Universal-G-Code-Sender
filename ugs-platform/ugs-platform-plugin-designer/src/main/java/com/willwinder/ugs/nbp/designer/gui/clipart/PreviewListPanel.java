/*
    Copyright 2022-2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.clipart;

import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class PreviewListPanel extends JPanel {
    private final transient List<ClipartSource> sources = new ArrayList<>();
    private final transient ActionListener selectAction;
    private final JPanel buttonsPanel = new JPanel();
    private final JScrollPane scrollPane;
    private transient Clipart selectedClipart;

    private static final List<String> FONT_SOURCES = Arrays.asList(
            "/fonts/christmas/mapping.json",
            "/fonts/your-sign/mapping.json",
            "/fonts/xmas/mapping.json",
            "/fonts/bu-dingbats/mapping.json",
            "/fonts/darrians-frames-font/mapping1.json",
            "/fonts/darrians-frames-font/mapping2.json",
            "/fonts/creepy-crawlies-font/mapping.json",
            "/fonts/house-icons/mapping.json",
            "/fonts/travelcons/mapping.json",
            "/fonts/tool/mapping.json",
            "/fonts/garden/mapping.json",
            "/fonts/sugar-coma-font/mapping.json",
            "/fonts/corners2/mapping.json",
            "/fonts/wwfreebie/mapping.json",
            "/fonts/destinys-borders/mapping.json",
            "/fonts/vintage-decorative-corners-23-font/mapping.json",
            "/fonts/vintage-decorative-signs-2-font/mapping.json",
            "/fonts/world-of-sci-fi-font/mapping.json",
            "/fonts/tropicana/mapping.json",
            "/fonts/transdings/mapping.json",
            "/fonts/sealife/mapping.json",
            "/fonts/logoskate-1/mapping.json",
            "/fonts/logoskate-2/mapping.json",
            "/fonts/mythical/mapping.json",
            "/fonts/komika-bubbles/mapping.json",
            "/fonts/fredoka-one/mapping.json",
            "/fonts/evilz/mapping.json",
            "/fonts/easterart/mapping.json",
            "/fonts/efon/mapping.json",
            "/fonts/eagle/mapping.json",
            "/fonts/black-white-banners/mapping.json",
            "/fonts/superhero/mapping.json",
            "/fonts/laurus-nobilis/mapping.json"
    );

    public PreviewListPanel(ActionListener selectAction) {
        this.selectAction = selectAction;
        buttonsPanel.setLayout(new MigLayout("fill, insets 10, wrap 4, top, left", "", ""));

        FONT_SOURCES.forEach(s -> {
            try {
                sources.add(new FontClipartSource(s));
            } catch (Exception e) {
                throw new ClipartSourceException("Could not load source " + s, e);
            }
        });

        setLayout(new BorderLayout());
        scrollPane = new JScrollPane(buttonsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setCategory(Category category) {
        buttonsPanel.removeAll();
        ClipartTooltip tooltip = new ClipartTooltip(this);
        sources.stream()
                .flatMap(source -> source.getCliparts(category).stream())
                .sorted(Comparator.comparing(clipart -> clipart.getName().toLowerCase()))
                .forEach(clipart -> createAndAddButton(tooltip, clipart));
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    private void createAndAddButton(ClipartTooltip tooltip, Clipart clipart) {
        ClipartButton roundedPanel = new ClipartButton(clipart, tooltip);
        roundedPanel.addClickListener(() -> {
            selectedClipart = clipart;
            selectAction.actionPerformed(new ActionEvent(roundedPanel, 0, "selected_clipart"));
        });
        buttonsPanel.add(roundedPanel, "grow, w 100:100:400");
    }

    public Optional<Clipart> getSelectedClipart() {
        return Optional.ofNullable(selectedClipart);
    }
}
