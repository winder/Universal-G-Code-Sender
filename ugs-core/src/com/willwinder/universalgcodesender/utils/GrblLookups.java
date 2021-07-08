/*
    Copyright 2016 Will Winder

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

import com.google.common.collect.Lists;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author wwinder
 */
public class GrblLookups {
    private HashMap<String,String[]> lookups = new HashMap<>();

    private static String pathFor(String filename) {
        return "/resources/grbl/" + filename;
    }

    public GrblLookups(String prefix) {
        String filename = prefix + "_" + Localization.loadedLocale() + ".csv";

        URL u = GrblLookups.class.getResource(pathFor(filename));
        if (u == null) {
            filename = prefix + "_en_US.csv";
        }

        try {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            GrblLookups.class.getResourceAsStream(pathFor(filename))))) {
                Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
                for (CSVRecord record : records) {
                  List<String> list = Lists.newArrayList(record.iterator());
                  lookups.put(record.get(0), list.toArray(new String[0]));
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
