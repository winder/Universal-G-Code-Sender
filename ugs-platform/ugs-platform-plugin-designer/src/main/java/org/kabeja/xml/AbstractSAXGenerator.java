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

import java.util.Map;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.processing.AbstractConfigurable;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public abstract class AbstractSAXGenerator extends AbstractConfigurable
    implements SAXGenerator {
    protected DXFDocument doc;
    protected ContentHandler handler;
    protected Map context;

    public void generate(DXFDocument doc, ContentHandler handler, Map context)
        throws SAXException {
        this.doc = doc;
        this.handler = handler;
        this.context = context;
        this.generate();
    }

    /**
     * This method has to be overwritten by any subclass. At this point the
     * XMLGenerator is setup (properties, ContentHandler and DXFDocument) and
     * should emit the XML content to the ContentHandler.
     *
     */
    protected abstract void generate() throws SAXException;
}
