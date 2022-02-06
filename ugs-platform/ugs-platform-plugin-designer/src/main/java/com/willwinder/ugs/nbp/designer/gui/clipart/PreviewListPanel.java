/*
    Copyright 2022 Will Winder

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

import com.willwinder.ugs.nbp.designer.gui.clipart.sources.*;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class PreviewListPanel extends JPanel {
    private final transient List<ClipartSource> sources = new ArrayList<>();
    private final transient ActionListener selectAction;
    private final JPanel buttonsPanel = new JPanel();
    private transient Clipart selectedClipart;

    public PreviewListPanel(ActionListener selectAction) {
        this.selectAction = selectAction;
        buttonsPanel.setLayout(new MigLayout("fill, insets 5, wrap 4, top, left", "", ""));
        sources.add(new Corners2Source());
        sources.add(new ChristmasSource());
        sources.add(new DestinysBordersSource());
        sources.add(new EasterArtSource());
        sources.add(new EfonSource());
        sources.add(new EvilzSource());
        sources.add(new FredokaSource());
        sources.add(new MythicalSource());
        sources.add(new KomikaBubblesSource());
        sources.add(new LogoSkate1Source());
        sources.add(new LogoSkate2Source());
        sources.add(new RemixIconSource());
        sources.add(new SealifeSource());
        sources.add(new TransdingsSource());
        sources.add(new TropicanaSource());
        sources.add(new YourSignSource());
        sources.add(new WwfreebieSource());
        sources.add(new XmasSource());

        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(buttonsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setCategory(Category category) {
        buttonsPanel.removeAll();
        sources.stream()
                .flatMap(source -> source.getCliparts(category).stream())
                .sorted((clipart1, clipart2) -> clipart1.getName().compareTo(clipart2.getName()))
                .forEach(clipart -> {
                    Component preview = clipart.getPreview();
                    RoundedPanel roundedPanel = new RoundedPanel(12);
                    roundedPanel.setLayout(new MigLayout("fill, inset 0"));
                    roundedPanel.setMinimumSize(new Dimension(128, 128));
                    roundedPanel.setForeground(ThemeColors.LIGHT_GREY);
                    roundedPanel.setBackground(Color.WHITE);
                    roundedPanel.setHoverBackground(ThemeColors.LIGHT_GREY);
                    roundedPanel.setPressedBackground(ThemeColors.VERY_LIGHT_BLUE_GREY);
                    roundedPanel.add(preview, "grow");
                    roundedPanel.addClickListener(() -> {
                        selectedClipart = clipart;
                        selectAction.actionPerformed(new ActionEvent(roundedPanel, 0, "selected_clipart"));
                    });
                    buttonsPanel.add(roundedPanel);
                });
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
    }

    public Optional<Clipart> getSelectedClipart() {
        return Optional.ofNullable(selectedClipart);
    }
}
