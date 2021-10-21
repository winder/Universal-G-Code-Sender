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
package org.kabeja.xml;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.helpers.XMLFilterImpl;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public abstract class AbstractSAXFilter extends XMLFilterImpl
    implements SAXFilter {
    protected Map properties = new HashMap();

    /* (non-Javadoc)
     * @see org.kabeja.xml.SAXFilter#setProperties(java.util.Map)
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public Map getProperties() {
        return this.properties;
    }
}
