/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.i18n.Localization;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author wwinder
 */
public class GrblLookups {
    String headers[] = null;
    HashMap<String,String[]> lookups = new HashMap<>();

    public GrblLookups(String prefix) {
        String filename = prefix + "_" + Localization.loadedLocale() + ".csv";

        URL u = GrblLookups.class.getClassLoader().getResource(filename);
        if (u == null) {
            filename = prefix + "_en_US.csv";
        }

        try {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            GrblLookups.class.getResourceAsStream(
                                    "/resources/grbl/" + filename)))) {
                headers = br.readLine().split(",");
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    lookups.put(parts[0], parts);
                }
            }
        } catch (IOException ex) {
            System.out.println("Unable to load GRBL resources.");
            ex.printStackTrace();
        }
    }

    public String[] lookup(String idx) {
        return lookups.get(idx);
    }
}
