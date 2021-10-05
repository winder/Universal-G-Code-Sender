/*
   Copyright 2008 Simon Mieth

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
package org.kabeja.parser.dxf.filter;

import org.kabeja.parser.DXFValue;
import org.kabeja.parser.ParseException;


abstract class DXFStreamSectionFilter extends AbstractDXFStreamFilter {
    private final static String SECTION_START = "SECTION";
    private final static String SECTION_END = "ENDSEC";
    private final static int COMMAND_CODE = 0;
    protected boolean sectionStarts = false;
    protected String section;

    public void parseGroup(int groupCode, DXFValue value)
        throws ParseException {
        if ((groupCode == COMMAND_CODE) &&
                SECTION_START.equals(value.getValue())) {
            sectionStarts = true;
        } else if (sectionStarts) {
            sectionStarts = false;
            section = value.getValue();
            sectionStart(section);
            parseSection(COMMAND_CODE, new DXFValue(SECTION_START));
            parseSection(groupCode, value);
        } else {
            parseSection(groupCode, value);
        }

        if ((groupCode == COMMAND_CODE) &&
                SECTION_END.equals(value.getValue())) {
            sectionEnd(section);
        }
    }

    protected abstract void parseSection(int groupCode, DXFValue value)
        throws ParseException;

    protected abstract void sectionStart(String Section)
        throws ParseException;

    protected abstract void sectionEnd(String Section)
        throws ParseException;
}
