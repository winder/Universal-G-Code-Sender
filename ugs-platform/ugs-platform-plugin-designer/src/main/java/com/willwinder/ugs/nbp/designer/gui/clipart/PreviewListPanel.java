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

import com.willwinder.ugs.nbp.designer.gui.clipart.sources.BuDingbatsSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.ChristmasSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.Corners2Source;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.CreepyCrawliesSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.DarriansFrames1Source;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.DarriansFrames2Source;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.DestinysBordersSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.EasterArtSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.EfonSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.EvilzSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.FredokaSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.GardenSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.HouseIconsSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.KomikaBubblesSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.LogoSkate1Source;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.LogoSkate2Source;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.MythicalSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.SealifeSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.SugarComaSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.ToolSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.TransdingsSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.TravelconsSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.TropicanaSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.VintageCorners23Source;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.VintageDecorativeSigns2Source;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.WorldOfScifiSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.WwfreebieSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.XmasSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.sources.YourSignSource;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
        sources.add(new SealifeSource());
        sources.add(new TransdingsSource());
        sources.add(new TropicanaSource());
        sources.add(new YourSignSource());
        sources.add(new WwfreebieSource());
        sources.add(new XmasSource());
        sources.add(new BuDingbatsSource());
        sources.add(new DarriansFrames1Source());
        sources.add(new DarriansFrames2Source());
        sources.add(new WorldOfScifiSource());
        sources.add(new VintageDecorativeSigns2Source());
        sources.add(new VintageCorners23Source());
        sources.add(new CreepyCrawliesSource());
        sources.add(new SugarComaSource());
        sources.add(new HouseIconsSource());
        sources.add(new TravelconsSource());
        sources.add(new ToolSource());
        sources.add(new GardenSource());


        setLayout(new BorderLayout());
        scrollPane = new JScrollPane(buttonsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setCategory(Category category) {
        buttonsPanel.removeAll();
        ClipartTooltip tooltip = new ClipartTooltip();
        sources.stream()
                .flatMap(source -> source.getCliparts(category).stream())
                .sorted(Comparator.comparing(clipart -> clipart.getName().toLowerCase()))
                .forEach(clipart -> {
                    ClipartButton roundedPanel = new ClipartButton(clipart, tooltip);
                    roundedPanel.addClickListener(() -> {
                        selectedClipart = clipart;
                        selectAction.actionPerformed(new ActionEvent(roundedPanel, 0, "selected_clipart"));
                    });
                    buttonsPanel.add(roundedPanel);
                });
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    public Optional<Clipart> getSelectedClipart() {
        return Optional.ofNullable(selectedClipart);
    }
}
