/*
   Copyright 2006 Simon Mieth

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

import java.util.Map;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.processing.Configurable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * This interface describes a generator component, which emit convert the
 * DXFDocument to SAX-Event.
 * <h3>Lifecycle</h3>
 *
 * <ol>
 * <li>setProperties</li>
 * <li>generate(DXFDocument doc,ConentHandler handler)</li>
 * </ol>
 *
 * @author simon.mieth
 *
 */
public interface SAXGenerator extends Configurable {
    public void generate(DXFDocument doc, ContentHandler handler, Map context)
        throws SAXException;
}
