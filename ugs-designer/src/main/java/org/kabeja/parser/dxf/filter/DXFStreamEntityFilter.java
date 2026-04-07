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


abstract class DXFStreamEntityFilter extends DXFStreamSectionFilter {
    private static String SECTION_KEY = "ENTITIES";
    public static final int ENTITY_START = 0;
    protected boolean entitySection = false;
    protected boolean parseEntity = false;
    protected boolean parseHeader = false;

    protected void parseSection(int groupCode, DXFValue value)
        throws ParseException {
        if (parseHeader) {
            if (value.getValue().equals(SECTION_KEY)) {
                this.entitySection = true;
                this.parseHeader = false;
            }
        } else if (entitySection) {
            if (groupCode == ENTITY_START) {
                if (parseEntity) {
                    endEntity();
                } else {
                    parseEntity = true;
                }

                startEntity(value.getValue());
            }

            parseEntity(groupCode, value);

            return;
        }

        handler.parseGroup(groupCode, value);
    }

    protected void sectionEnd(String Section) throws ParseException {
        if (section.equals(SECTION_KEY)) {
            this.entitySection = false;
        }
    }

    protected void sectionStart(String Section) throws ParseException {
        if (section.equals(SECTION_KEY)) {
            this.parseHeader = true;
        }
    }

    protected abstract void startEntity(String type) throws ParseException;

    protected abstract void endEntity() throws ParseException;

    protected abstract void parseEntity(int groupCode, DXFValue value)
        throws ParseException;
}
