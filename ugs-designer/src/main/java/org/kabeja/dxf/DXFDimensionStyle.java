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
package org.kabeja.dxf;

import java.util.HashMap;
import java.util.Iterator;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFDimensionStyle {
    /**
     * the size of the dimension text
     */
    public static final String PROPERTY_DIMTXT = "140";

    /**
     * the leader arrow block
     */
    public static final String PROPERTY_DIMLDRBLK = "341";

    /**
     * the color of dimensionline, arrow and helpline
     */
    public static final String PROPERTY_DIMCLRD = "176";
    public static final String PROPERTY_DIMASZ = "41";
    public static final String PROPERTY_DIMGAP = "147";
    public static final String PROPERTY_DIMSCALE = "40";

    /**
     * the textstyle of the dimensiontext
     */
    public static final String PROPERTY_DIMTXSTY = "340";
    public static final String PROPERTY_DIMLWD = "371";
    public static final String PROPERTY_DIMADEC = "179";
    public static final String PROPERTY_DIMALT = "170";
    public static final String PROPERTY_DIMALTD = "171";
    public static final String PROPERTY_DIMALTF = "143";
    public static final String PROPERTY_DIMALTRND = "148";
    public static final String PROPERTY_DIMALTTD = "274";
    public static final String PROPERTY_DIMALTTZ = "286";
    public static final String PROPERTY_DIMALTU = "273";
    public static final String PROPERTY_DIMALTZ = "285";
    public static final String PROPERTY_DIMAPOST = "4";
    public static final String PROPERTY_DIMATFIT = "289";
    public static final String PROPERTY_DIMUNIT = "275";
    public static final String PROPERTY_DIMAZIN = "79";

    /**
     * the arrow block
     */
    public static final String PROPERTY_DIMBLK = "342";

    /**
     * the first or left arrow block (if different blocks used)
     */
    public static final String PROPERTY_DIMBLK1 = "343";

    /**
     * the second or right arrow block (if different blocks used)
     */
    public static final String PROPERTY_DIMBLK2 = "344";
    public static final String PROPERTY_DIMCEN = "141";
    public static final String PROPERTY_DIMCLRE = "177";
    public static final String PROPERTY_DIMCLRT = "178";
    public static final String PROPERTY_DIMDEC = "271";
    public static final String PROPERTY_DIMDLE = "46";
    public static final String PROPERTY_DIMDLI = "43";
    public static final String PROPERTY_DIMDSEP = "278";
    public static final String PROPERTY_DIMEXE = "44";
    public static final String PROPERTY_DIMEXO = "42";
    public static final String PROPERTY_DIMFRAC = "276";
    public static final String PROPERTY_DIMJUST = "280";
    public static final String PROPERTY_DIMLFAC = "144";
    public static final String PROPERTY_DIMLIM = "72";
    public static final String PROPERTY_DIMLUNIT = "277";
    public static final String PROPERTY_DIMLWE = "372";
    public static final String PROPERTY_DIMPOST = "3";
    public static final String PROPERTY_DIMMD = "45";
    public static final String PROPERTY_DIMSAH = "173";
    public static final String PROPERTY_DIMSD1 = "281";
    public static final String PROPERTY_DIMSD2 = "282";
    public static final String PROPERTY_DIMSE1 = "75";
    public static final String PROPERTY_DIMSE2 = "76";
    public static final String PROPERTY_DIMSOXD = "175";
    public static final String PROPERTY_DIMRAD = "77";
    public static final String PROPERTY_DIMTDEC = "272";
    public static final String PROPERTY_DIMDTFAC = "146";
    public static final String PROPERTY_DIMTIH = "73";
    public static final String PROPERTY_DIMTIX = "174";
    public static final String PROPERTY_DIMDIMTM = "48";
    public static final String PROPERTY_DIMTMOVE = "289";
    public static final String PROPERTY_DIMTOFL = "172";
    public static final String PROPERTY_DIMTOH = "74";
    public static final String PROPERTY_DIMTOL = "71";
    public static final String PROPERTY_DIMTOLJ = "283";
    public static final String PROPERTY_DIMTP = "47";
    public static final String PROPERTY_DIMTSZ = "142";
    public static final String PROPERTY_DIMTVP = "145";
    public static final String PROPERTY_DIMTZIN = "284";
    public static final String PROPERTY_DIMZIN = "78";
    private HashMap properties = new HashMap();
    private int flags = 0;
    private String name = "";

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public String getProperty(String name) {
        return (String) properties.get(name);
    }

    public int getIntegerProperty(String name) {
        String value = (String) properties.get(name);

        return Integer.parseInt(value);
    }

    public int getIntegerProperty(String name, int defaultValue) {
        if (hasProperty(name)) {
            String value = (String) properties.get(name);

            return Integer.parseInt(value);
        } else {
            return defaultValue;
        }
    }

    public double getDoubleProperty(String name) {
        String value = (String) properties.get(name);

        return Double.parseDouble(value);
    }

    public double getDoubleProperty(String name, double defaultValue) {
        if (hasProperty(name)) {
            String value = (String) properties.get(name);

            return Double.parseDouble(value);
        } else {
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String name) {
        String value = (String) properties.get(name);

        if ("1".equals(value)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        if (hasProperty(name)) {
            String value = (String) properties.get(name);

            if ("1".equals(value)) {
                return true;
            } else {
                return false;
            }
        } else {
            return defaultValue;
        }
    }

    public Iterator getPropertyIterator() {
        return properties.values().iterator();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getFlags() {
        return flags;
    }
}
