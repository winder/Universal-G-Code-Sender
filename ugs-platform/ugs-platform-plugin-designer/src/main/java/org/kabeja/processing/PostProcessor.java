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
package org.kabeja.processing;

import java.util.Map;

import org.kabeja.dxf.DXFDocument;


/**
 * This interface describes a PostPorcessor, which will work direct with
 * parsed CAD data.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>setProperties()</li>
 *   <li>process()</li>
 *
 * </ol>
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public interface PostProcessor extends Configurable {
    /**
     * Postprocess the given DXFDocument
     * @param doc
     * @param context
     * @throws ProcessorException
     */
    public void process(DXFDocument doc, Map context) throws ProcessorException;
}
