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
package org.kabeja.parser.dxf.filter;

import java.util.Map;

import org.kabeja.parser.dxf.DXFHandler;


/**
 *
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public interface DXFStreamFilter extends DXFHandler {
    /**
     * The next DXFHandler in the chain.
     * @param handler
     */
    public void setDXFHandler(DXFHandler handler);

    /**
     * Setup properties for the DXFStreamFilter. Will called before the parsing
     * starts.
     * @param properties
     */
    public void setProperties(Map properties);
}
