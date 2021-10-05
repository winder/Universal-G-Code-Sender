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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.kabeja.parser.DXFValue;
import org.kabeja.parser.ParseException;


public class DXFStreamLayerFilter extends DXFStreamEntityFilter {
    public final static String PROPERTY_LAYERS_EXCLUDE = "layers.exclude";
    public final static String PROPERTY_LAYERS_INCLUDE = "layers.include";
    public final static int LAYER_NAME = 8;
    protected List parseValues = new ArrayList();
    protected Set exclude = new HashSet();
    protected Set include = new HashSet();
    protected String layer = "";
    boolean findLayer = true;

    public void setProperties(Map properties) {
        if (properties.containsKey(PROPERTY_LAYERS_INCLUDE)) {
            this.include.clear();

            StringTokenizer st = new StringTokenizer((String) properties.get(
                        PROPERTY_LAYERS_INCLUDE), "|");

            while (st.hasMoreTokens()) {
                String layer = st.nextToken();

                this.include.add(layer);
            }
        }

        if (properties.containsKey(PROPERTY_LAYERS_EXCLUDE)) {
            this.exclude.clear();

            StringTokenizer st = new StringTokenizer((String) properties.get(
                        PROPERTY_LAYERS_EXCLUDE), "|");

            while (st.hasMoreTokens()) {
                this.exclude.add(st.nextToken());
            }
        }
    }

    protected void endEntity() throws ParseException {
        if (include.contains(this.layer)) {
            this.outputEntity();
        } else if (!exclude.contains(this.layer)) {
            this.outputEntity();
        }
    }

    protected void outputEntity() throws ParseException {
        // give the complete entity to the next handler
        for (int i = 0; i < this.parseValues.size(); i++) {
            ParseValue v = (ParseValue) this.parseValues.get(i);
            this.handler.parseGroup(v.getGroupCode(), v.getDXFValue());
        }
    }

    protected void startEntity(String type) throws ParseException {
        this.parseValues.clear();
        this.findLayer = true;
    }

    protected void parseEntity(int groupCode, DXFValue value)
        throws ParseException {
        if (this.findLayer && (groupCode == LAYER_NAME)) {
            this.layer = value.getValue();
            this.findLayer = false;
        }

        //parse values to buffer
        ParseValue v = new ParseValue(groupCode, value);
        this.parseValues.add(v);
    }

    private class ParseValue {
        int groupCode;
        DXFValue value;

        public ParseValue(int groupCode, DXFValue value) {
            this.groupCode = groupCode;
            this.value = value;
        }

        public int getGroupCode() {
            return this.groupCode;
        }

        public DXFValue getDXFValue() {
            return this.value;
        }
    }
}
