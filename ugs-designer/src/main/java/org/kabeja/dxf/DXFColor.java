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


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 *
 *
 */
public class DXFColor {
    private static int[][] rgbs = {
            { 255, 0, 0 },
            { 255, 255, 0 },
            { 0, 255, 0 },
            { 0, 255, 255 },
            { 0, 0, 255 },
            { 255, 0, 255 },
            { 0, 0, 0 },
            { 128, 128, 128 },
            { 192, 192, 192 },
            { 255, 1, 1 },
            { 255, 127, 127 },
            { 165, 0, 0 },
            { 165, 82, 82 },
            { 127, 0, 0 },
            { 127, 63, 63 },
            { 76, 0, 0 },
            { 76, 38, 38 },
            { 38, 0, 0 },
            { 38, 19, 19 },
            { 255, 63, 0 },
            { 255, 159, 127 },
            { 165, 41, 0 },
            { 165, 103, 82 },
            { 127, 31, 0 },
            { 127, 79, 63 },
            { 76, 19, 0 },
            { 76, 47, 38 },
            { 38, 9, 0 },
            { 38, 23, 19 },
            { 255, 127, 0 },
            { 255, 191, 127 },
            { 165, 82, 0 },
            { 165, 124, 82 },
            { 127, 63, 0 },
            { 127, 95, 63 },
            { 76, 38, 0 },
            { 76, 57, 38 },
            { 38, 19, 0 },
            { 38, 28, 19 },
            { 255, 191, 0 },
            { 255, 223, 127 },
            { 165, 124, 0 },
            { 165, 145, 82 },
            { 127, 95, 0 },
            { 127, 111, 63 },
            { 76, 57, 0 },
            { 76, 66, 38 },
            { 38, 28, 0 },
            { 38, 33, 19 },
            { 255, 255, 1 },
            { 255, 255, 127 },
            { 165, 165, 0 },
            { 165, 165, 82 },
            { 127, 127, 0 },
            { 127, 127, 63 },
            { 76, 76, 0 },
            { 76, 76, 38 },
            { 38, 38, 0 },
            { 38, 38, 19 },
            { 191, 255, 0 },
            { 223, 255, 127 },
            { 124, 165, 0 },
            { 145, 165, 82 },
            { 95, 127, 0 },
            { 111, 127, 63 },
            { 57, 76, 0 },
            { 66, 76, 38 },
            { 28, 38, 0 },
            { 33, 38, 19 },
            { 127, 255, 0 },
            { 191, 255, 127 },
            { 82, 165, 0 },
            { 124, 165, 82 },
            { 63, 127, 0 },
            { 95, 127, 63 },
            { 38, 76, 0 },
            { 57, 76, 38 },
            { 19, 38, 0 },
            { 28, 38, 19 },
            { 63, 255, 0 },
            { 159, 255, 127 }
        };
    private static final String DEFAULT_COLOR = "0,0,0";

    public static String getRGBString(int dxfColorCode) {
        if ((dxfColorCode > 0) && ((dxfColorCode - 1) <= (rgbs.length - 1))) {
            // the StringBuffer is faster then the String concat
            StringBuffer buf = new StringBuffer();
            buf.append(rgbs[dxfColorCode - 1][0]);
            buf.append(",");
            buf.append(rgbs[dxfColorCode - 1][1]);
            buf.append(",");
            buf.append(rgbs[dxfColorCode - 1][2]);

            return buf.toString();
        }

        // default is black
        return DEFAULT_COLOR;
    }
}
