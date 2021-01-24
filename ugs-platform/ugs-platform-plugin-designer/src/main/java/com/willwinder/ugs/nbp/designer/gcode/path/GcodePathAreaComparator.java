package com.willwinder.ugs.nbp.designer.gcode.path;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GcodePathAreaComparator implements Comparator<GcodePath> {

    private Map<GcodePath, Double> areaCache;

    public GcodePathAreaComparator(List<GcodePath> sources) {
        this.areaCache = new IdentityHashMap<>(sources.size());
    }

    @Override
    public int compare(GcodePath p1, GcodePath p2) {
        double a1 = calcArea(p1);
        double a2 = calcArea(p2);
        return Double.compare(a1, a2);
    }

    /**
     * Calculate the area of a path bounding rectangle
     */
    private double calcArea(GcodePath gcodePath) {
        if (areaCache.containsKey(gcodePath)) {
            return areaCache.get(gcodePath);
        }

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Segment s : gcodePath.getSegments()) {
            if (s.point != null) {
                double x = s.point.get(Axis.X);
                double y = s.point.get(Axis.Y);
                if (!Double.isNaN(x)) {
                    if (x < minX)
                        minX = x;
                    if (x > maxX)
                        maxX = x;
                }

                if (!Double.isNaN(y)) {
                    if (y < minY)
                        minY = y;
                    if (y > maxY)
                        maxY = y;
                }
            }
        }

        double area = (maxX - minX) * (maxY - minY);
        areaCache.put(gcodePath, area);
        return area;
    }
}
