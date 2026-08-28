/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

public class TabsTest {

    @Test
    public void split_shouldLeaveEvenlySpacedTabsAlongTheLine() {
        List<PartialPosition> line = line(0, 100);

        List<Tabs.Section> sections = Tabs.split(line, 2, 10);

        Assertions.assertThat(sections).hasSize(5);
        Assertions.assertThat(sections.stream().map(Tabs.Section::tab))
                .containsExactly(false, true, false, true, false);
        Assertions.assertThat(startOf(sections.get(1))).isEqualTo(20, Assertions.within(0.001));
        Assertions.assertThat(endOf(sections.get(1))).isEqualTo(30, Assertions.within(0.001));
        Assertions.assertThat(startOf(sections.get(3))).isEqualTo(70, Assertions.within(0.001));
        Assertions.assertThat(endOf(sections.get(3))).isEqualTo(80, Assertions.within(0.001));
    }

    @Test
    public void split_shouldRepeatTheBoundaryPositionInBothSections() {
        List<PartialPosition> line = line(0, 100);

        List<Tabs.Section> sections = Tabs.split(line, 1, 10);

        Assertions.assertThat(endOf(sections.get(0))).isEqualTo(startOf(sections.get(1)));
        Assertions.assertThat(endOf(sections.get(1))).isEqualTo(startOf(sections.get(2)));
    }

    @Test
    public void split_shouldShortenTabsThatWouldNotFitAlongTheLine() {
        List<PartialPosition> line = line(0, 10);

        List<Tabs.Section> sections = Tabs.split(line, 2, 10);

        // Half of the line is left to be cut, leaving 2.5mm for each of the two tabs
        Assertions.assertThat(endOf(sections.get(1)) - startOf(sections.get(1))).isEqualTo(2.5, Assertions.within(0.001));
        Assertions.assertThat(endOf(sections.get(3)) - startOf(sections.get(3))).isEqualTo(2.5, Assertions.within(0.001));
    }

    @Test
    public void split_shouldKeepThePathWhenTheTabsWouldBeTooShortToHold() {
        List<PartialPosition> line = line(0, 0.01);

        List<Tabs.Section> sections = Tabs.split(line, 4, 10);

        Assertions.assertThat(sections).hasSize(1);
        Assertions.assertThat(sections.get(0).tab()).isFalse();
    }

    @Test
    public void split_shouldKeepThePathWhenThereAreNoTabs() {
        List<PartialPosition> line = line(0, 100);

        List<Tabs.Section> sections = Tabs.split(line, 0, 10);

        Assertions.assertThat(sections).hasSize(1);
        Assertions.assertThat(sections.get(0).coordinates()).isEqualTo(line);
    }

    private static List<PartialPosition> line(double from, double to) {
        return List.of(
                new PartialPosition(from, 0d, UnitUtils.Units.MM),
                new PartialPosition(to, 0d, UnitUtils.Units.MM));
    }

    private static double startOf(Tabs.Section section) {
        return section.coordinates().get(0).getX();
    }

    private static double endOf(Tabs.Section section) {
        return section.coordinates().get(section.coordinates().size() - 1).getX();
    }
}
