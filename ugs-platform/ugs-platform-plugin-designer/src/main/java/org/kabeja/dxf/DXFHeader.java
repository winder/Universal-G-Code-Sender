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

import java.util.Hashtable;
import java.util.Iterator;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 *
 *
 */
public class DXFHeader {
    private Hashtable variables = new Hashtable();

    public DXFHeader() {
    }

    public void setVariable(DXFVariable v) {
        variables.put(v.getName(), v);
    }

    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }

    public DXFVariable getVariable(String name) {
        return (DXFVariable) variables.get(name);
    }

    public Iterator getVarialbeIterator() {
        return variables.values().iterator();
    }

    public boolean isFillMode() {
        if (hasVariable("$FILLMODE") &&
                (getVariable("$FILLMODE").getDoubleValue("70") > 0)) {
            return true;
        }

        return false;
    }

    /**
     * Returns the global linetype scale factor.
     *
     * @return the global scalefactor
     */
    public double getLinetypeScale() {
        double gscale = 1.0;

        if (hasVariable("$LTSCALE")) {
            gscale = getVariable("$LTSCALE").getDoubleValue("40");
        }

        return gscale;
    }
}
