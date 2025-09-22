/*
 * Copyright (C) 2025 dimic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.platform.surfacer;

import com.willwinder.ugs.platform.surfacer.GeneratePath.Point;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author dimic
 */
public class Gcode {
 
    private static final int SPINUP_DELAY_SEC = 2;
    private static final int COMMENT_POSITION = 26;
    
    private final DecimalFormat DF = new DecimalFormat("0.#####");
    
    private Prefs prefs;
    private BackendAPI backend;
    private File file = null;
    private final GeneratePath generatePath = new GeneratePath();
    
    private TextFieldUnit units;
    private double xmin, ymin, xmax, ymax;
    
    public void init(Prefs prefs, BackendAPI backend) {
        this.prefs = Objects.requireNonNull(prefs);
        this.backend = Objects.requireNonNull(backend);
    }

    public File getFile() { return file; }

    public void generate(File _file) {
        units = backend.getSettings().getPreferredUnits() ==
            UnitUtils.Units.MM ? TextFieldUnit.MM : TextFieldUnit.INCH;

        generatePath.init(prefs);
        
        xmin = generatePath.xmin; ymin = generatePath.ymin;
        xmax = generatePath.xmax; ymax = generatePath.ymax;

        // build lines of gcode
        List<String> lines = new ArrayList<>();
        beginGcode(lines);
        gcodePath(lines);
        endGcode(lines);
        
        if (_file == null) {
            try {
              file = Files.createTempFile("surfacer", ".gc").toFile();
            } catch (IOException e) {
              GUIHelpers.displayErrorDialog("Error loading file: " + e.getLocalizedMessage());
            }
        } else {
            file = _file;
        }
        
        try {
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            GUIHelpers.displayErrorDialog("Error writing file: " + e.getLocalizedMessage());
        }
    }

    private boolean isMM() { return units == TextFieldUnit.MM; }
    private double prefConv(double value) { return isMM() ? value : value / 25.4; }
    private String prefUnits(double value) {
        return String.format("%s %s", DF.format(prefConv(value)), isMM() ? "mm" : "in");
    }

    private void beginGcode(List<String> lines) {
        addGLine(lines, isMM() ? "G21" : "G20", isMM() ? "mm" : "in");
        addGLine(lines, "G90", "absolute coordinates");
        addGLine(lines, "G94", "units per minute feedrates");
        lines.add("");
        lines.add(String.format("; Tool diameter: %s", prefUnits(prefs.toolDiameter())));
        lines.add(String.format("; Spindle speed: %.0f rpm", prefs.spindleSpeed()));
        lines.add(String.format("; Overlap      : %.0f%%", prefs.overlap()*100));
        lines.add(String.format("; X0, Y0       : %s, %s", prefUnits(prefs.x0()), prefUnits(prefs.y0())));
        lines.add(String.format("; X1, Y1       : %s, %s", prefUnits(prefs.x1()), prefUnits(prefs.y1())));
        lines.add(String.format("; XY feedrate  : %s/min", prefUnits(prefs.xyFeedrate())));
        lines.add(String.format("; Depth        : %s", prefUnits(prefs.depth())));
        lines.add(String.format("; Cut step     : %s", prefUnits(prefs.cutStep())));
        lines.add(String.format("; Plunge rate  : %s/min", prefUnits(prefs.plungeRate())));
        lines.add("");
        addZLine(lines, "G0", prefs.zSafe());
        addSLine(lines, "M3", prefs.spindleSpeed(), "start spindle");
        addPLine(lines, "G4", SPINUP_DELAY_SEC, String.format("%dsec spin-up pause", SPINUP_DELAY_SEC));
        lines.add("");
    }

    private void gcodePath(List<String> lines) {
        List<Point> path;
        switch (prefs.pattern()) {
            case 1 -> {
                path = generatePath.spiralPath();
            }
            default -> path = generatePath.rasterPath();
        }
        double zstart = prefs.zStart();
        double zcut = zstart + prefs.cutStep();   // cutStep() is negative
        double cutStop = prefs.depth() - (prefs.finishCount() == 0 ? 0 : prefs.finishCut());
        
        while (true) {
            if (zcut < cutStop) zcut = cutStop;
            pathToLines(path, lines, zstart, zcut, prefs.xyFeedrate());
            if (zcut <= cutStop) break;
            zstart = zcut;
            zcut += prefs.cutStep();
        }
        if (zcut != cutStop) pathToLines(path, lines, zcut, cutStop, prefs.xyFeedrate());
        
        // we might have some finish passes to make
        if (prefs.finishCount() > 0 && prefs.finishCut() != 0) {
            for (int i=0; i<prefs.finishCount(); i++) {
                pathToLines(path, lines, zcut, prefs.depth(), prefs.finishFeedrate());
            }
        }
    }
    
    private void pathToLines(List<Point> path, List<String> lines, double zstart, double zcut, double feedrate) {
        lines.add(String.format("; %s %s depth pass", DF.format(zcut), isMM() ? "mm" : "in"));
        boolean newLine = true, atZstart = false, shorthand = false;
        
        for (Point p: path) {
            if (p == null) {    // pen up
                addZLine(lines, "G0", zstart);
                newLine = atZstart = true;
                continue;
            }

            if (newLine) {      // move to p and pen down
                addXYLine(lines, "G0", p.x, p.y);
                if (!atZstart) addZLine (lines, "G0", zstart);
                addZLine(lines, "G1", zcut, prefs.plungeRate());
                atZstart = newLine = shorthand = false;
                continue;
            }

            if (!shorthand) {   // draw to p
                addXYLine(lines, "G1", p.x, p.y, feedrate);
                shorthand = true;
            } else {
                addXYLine(lines, "  ", p.x, p.y);
            }
        }
        //if (outline) outlinePath(lines, zstart, zcut, feedrate);
        addZLine(lines, "G0", prefs.zSafe());
        lines.add("");
    }
    
    private void endGcode(List<String> lines) {
        addGLine (lines, "M5", "stop spindle");
        addXYLine(lines, "G0", xmin, ymin);
    }
    
    private void addGLine (List<String> lines, String command, String comment)               { lines.add(formatLine(command, null, null, null, null, null, null, comment)); }
    private void addPLine (List<String> lines, String command, Integer p, String comment)    { lines.add(formatLine(command, null, null, null, null,    p, null, comment)); }
    private void addSLine (List<String> lines, String command, Double s, String comment)     { lines.add(formatLine(command, null, null, null,    s, null, null, comment)); }
    private void addZLine (List<String> lines, String command, Double z)                     { lines.add(formatLine(command, null, null,    z, null, null, null,    null)); }
    private void addZLine (List<String> lines, String command, Double z, Double f)           { lines.add(formatLine(command, null, null,    z, null, null,    f,    null)); }
    private void addXYLine(List<String> lines, String command, Double x, Double y)           { lines.add(formatLine(command,    x,    y, null, null, null, null,    null)); }
    private void addXYLine(List<String> lines, String command, Double x, Double y, Double f) { lines.add(formatLine(command,    x,    y, null, null, null,    f,    null)); }

    private String formatLine(String command, Double x, Double y, Double z, Double s, Integer p, Double f, String comment) {
        String out = command;
        if (x != null) out += String.format(" X%.6f", prefConv(x));
        if (y != null) out += String.format(" Y%.6f", prefConv(y));
        if (z != null) out += String.format(" Z%.6f", prefConv(z));
        if (s != null) out += String.format(" S%.0f", s);
        if (p != null) out += String.format(" P%d", p);
        if (f != null) out += String.format(" F%.0f", prefConv(f));
        if (comment != null) {
            int pad = COMMENT_POSITION - out.length();
            if (pad > 0) out += " ".repeat(pad);
            out += " ; " + comment;
        }
        return out;
    }
}
