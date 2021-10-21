/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja.tools;

import java.io.BufferedReader;
import java.io.IOException;

import org.kabeja.dxf.DXFConstants;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class CodePageParser {
    public static final String CODEPAGE_CODE = "$DWGCODEPAGE";
    public static final String GROUPCODE = "3";
    private static final String[] prefix = { "ansi_", "dos" };
    private static final String javaPrefix = "Cp";

    public String parseEncoding(BufferedReader reader) {
        String encoding = "";

        try {
            String line = null;
            String code;
            String value;
            boolean next = true;
            boolean codepage = false;
            boolean key = true;
            String currentKey = null;

            while (((line = reader.readLine()) != null) && next) {
                line = line.trim();

                if (key) {
                    currentKey = line;
                    key = false;
                } else {
                    key = true;

                    // we read the first section
                    if (DXFConstants.SECTION_END.equals(line)) {
                        return encoding;
                    } else if (CODEPAGE_CODE.equals(line)) {
                        codepage = true;
                    } else if (codepage && currentKey.equals("3")) {
                        // the encoding
                        return translateCodePage(line);
                    } else if (DXFConstants.SECTION_CLASSES.equals(line) ||
                            DXFConstants.SECTION_BLOCKS.equals(line) ||
                            DXFConstants.SECTION_ENTITIES.equals(line)) {
                        return encoding;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encoding;
    }

    public String translateCodePage(String cp) {
        String c = cp.toLowerCase();

        for (int i = 0; i < prefix.length; i++) {
            if (c.startsWith(prefix[i])) {
                return javaPrefix + cp.substring(prefix[i].length());
            }
        }

        return cp;
    }
}
