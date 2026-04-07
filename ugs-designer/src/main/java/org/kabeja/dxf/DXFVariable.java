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
public class DXFVariable {
    private Hashtable values = new Hashtable();
    private String name = "";

    public DXFVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue(String name) {
        return (String) values.get(name);
    }

    public int getIntegerValue(String name) {
        return Integer.parseInt((String) values.get(name));
    }

    public double getDoubleValue(String name) {
        return Double.parseDouble((String) values.get(name));
    }

    public void setValue(String name, String value) {
        values.put(name, value);
    }

    /**
     *
     * @return a iterator over all keys of this DXFValue
     */
    public Iterator getValueKeyIterator() {
        return values.keySet().iterator();
    }
}
