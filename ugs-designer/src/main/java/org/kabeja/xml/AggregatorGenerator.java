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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kabeja.dxf.DXFDocument;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


public class AggregatorGenerator extends AbstractSAXFilter
    implements SAXGenerator {
    public final static String ROOT_ELEMENT = "aggregate";
    public final static String NAMESPACE = "http://kabeja.org/aggregate";
    protected List generators = new ArrayList();
    protected DXFDocument doc;

    public void generate(DXFDocument doc, ContentHandler handler, Map context)
        throws SAXException {
        this.setContentHandler(handler);
        this.doc = doc;

        try {
            handler.startDocument();

            String raw = NAMESPACE + ":" + ROOT_ELEMENT;
            handler.startElement(NAMESPACE, raw, ROOT_ELEMENT,
                new AttributesImpl());
            doGenerate();
            handler.endElement(NAMESPACE, raw, ROOT_ELEMENT);
            handler.endDocument();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    protected void doGenerate() throws SAXException {
        Iterator i = this.generators.iterator();

        while (i.hasNext()) {
            SAXGenerator generator = (SAXGenerator) i.next();
            generator.generate(this.doc, this, null);
        }
    }

    public void endDocument() throws SAXException {
        // ignore
    }

    public void startDocument() throws SAXException {
        // ignore
    }

    public void addSAXGenerator(SAXGenerator generator) {
        this.generators.add(generator);
    }

    public Map getProperties() {
        return this.properties;
    }
}
