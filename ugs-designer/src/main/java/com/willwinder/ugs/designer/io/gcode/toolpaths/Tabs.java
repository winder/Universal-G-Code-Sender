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

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a tool path into the stretches that are cut all the way and the stretches that are left as
 * tabs holding the shape in the stock.
 * <p>
 * The tabs are spread out evenly along the path with the first one starting half a spacing in, so
 * that no tab lands on the seam where the path begins and ends. A path that is too short to fit the
 * requested tabs at their full length gets shorter ones instead of fewer, keeping every tab clear of
 * its neighbours.
 *
 * @author Joacim Breiler
 */
public final class Tabs {
    /**
     * A tab shorter than this in millimeters would be cut away by the tool passing over it, so the
     * path is left without tabs instead.
     */
    private static final double MINIMUM_TAB_LENGTH = 0.01;

    private Tabs() {
    }

    /**
     * Splits the given tool path into alternating cut and tab sections, always beginning and ending
     * with a cut section. The position where a section ends is repeated as the first position of the
     * next one, so that the sections can be cut one after the other without a gap between them.
     *
     * @param coordinates  the positions of the tool path, in order
     * @param tabCount     how many tabs to spread out along the path
     * @param maxTabLength how long a tab may be in millimeters, measured along the path
     * @return the sections of the path, or the whole path as a single cut section if it can not hold
     * any tabs
     */
    public static List<Section> split(List<PartialPosition> coordinates, int tabCount, double maxTabLength) {
        if (coordinates.size() < 2 || tabCount < 1) {
            return List.of(new Section(List.copyOf(coordinates), false));
        }

        double[] segmentLengths = segmentLengths(coordinates);
        double totalLength = 0;
        for (double segmentLength : segmentLengths) {
            totalLength += segmentLength;
        }

        // Half of the path is left for the cuts between the tabs, so that the tabs never meet
        double tabLength = Math.min(maxTabLength, totalLength / (2d * tabCount));
        if (tabLength < MINIMUM_TAB_LENGTH) {
            return List.of(new Section(List.copyOf(coordinates), false));
        }

        return split(coordinates, segmentLengths, boundaries(tabCount, totalLength, tabLength));
    }

    /**
     * The positions along the path where a tab begins or ends, in ascending order and alternating
     * between the two.
     */
    private static double[] boundaries(int tabCount, double totalLength, double tabLength) {
        double[] boundaries = new double[tabCount * 2];
        double spacing = totalLength / tabCount;
        for (int i = 0; i < tabCount; i++) {
            double center = (i + 0.5) * spacing;
            boundaries[i * 2] = center - tabLength / 2d;
            boundaries[i * 2 + 1] = center + tabLength / 2d;
        }
        return boundaries;
    }

    private static List<Section> split(List<PartialPosition> coordinates, double[] segmentLengths, double[] boundaries) {
        List<Section> sections = new ArrayList<>();
        List<PartialPosition> current = new ArrayList<>();
        current.add(coordinates.get(0));

        boolean isTab = false;
        int boundaryIndex = 0;
        double position = 0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            PartialPosition from = coordinates.get(i);
            PartialPosition to = coordinates.get(i + 1);
            double segmentLength = segmentLengths[i];

            while (segmentLength > 0 && boundaryIndex < boundaries.length && boundaries[boundaryIndex] <= position + segmentLength) {
                PartialPosition boundary = interpolate(from, to, (boundaries[boundaryIndex] - position) / segmentLength);
                current.add(boundary);
                sections.add(new Section(List.copyOf(current), isTab));

                isTab = !isTab;
                current = new ArrayList<>();
                current.add(boundary);
                boundaryIndex++;
            }

            current.add(to);
            position += segmentLength;
        }

        sections.add(new Section(List.copyOf(current), isTab));
        return sections;
    }

    private static double[] segmentLengths(List<PartialPosition> coordinates) {
        double[] lengths = new double[coordinates.size() - 1];
        for (int i = 0; i < lengths.length; i++) {
            PartialPosition from = coordinates.get(i);
            PartialPosition to = coordinates.get(i + 1);
            lengths[i] = Math.hypot(to.getX() - from.getX(), to.getY() - from.getY());
        }
        return lengths;
    }

    private static PartialPosition interpolate(PartialPosition from, PartialPosition to, double ratio) {
        return PartialPosition.builder(from)
                .setX(from.getX() + (to.getX() - from.getX()) * ratio)
                .setY(from.getY() + (to.getY() - from.getY()) * ratio)
                .build();
    }

    /**
     * A stretch of a tool path that is either cut or left as a tab.
     *
     * @param coordinates the positions of the stretch, in order
     * @param tab         true if the stretch is a tab that should not be cut through
     */
    public record Section(List<PartialPosition> coordinates, boolean tab) {
    }
}
