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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * <p>
 * This outputs a SAXStream to an OutputStream with the given encoding or
 * otherwise with the default encoding (utf-8).
 * </p>
 * <p>
 * <b>Note: </b> Not all features are implemented, so if you use this with other
 * SAXStreams others then the Kabeja-SAXStream you will get broken
 * XML-Documents.
 * </p>
 *
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class SAXPrettyOutputter extends AbstractSAXSerializer
    implements SAXSerializer {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String SUFFIX = "svg";
    public static final String SUFFIX_GZIP = "svgz";
    public static final String MIMETYPE = "text/svg";
    public static final String PROPERTY_ENCODING = "encoding";
    public static final String PROPERTY_GZIP = "gzip";
    private OutputStreamWriter out;
    private String encoding;
    private String dtd;
    private int indent = 0;
    private boolean parent = false;
    private ArrayList textContentList = new ArrayList();
    protected HashMap rootxmlns = new HashMap();
    protected boolean gzip = false;

    public SAXPrettyOutputter(OutputStream output, String encoding) {
        this.encoding = encoding;
        this.setOutput(output);
    }

    /**
     *
     */
    public SAXPrettyOutputter(OutputStream out) {
        this(out, DEFAULT_ENCODING);
    }

    public SAXPrettyOutputter() {
        this.encoding = DEFAULT_ENCODING;
    }

    public void characters(char[] ch, int start, int length)
        throws SAXException {
        try {
            if (length > 0) {
                if (parent) {
                    this.out.write(">");
                    parent = false;
                }

                char[] enc = encodeXML(new String(ch, 0, length)).toCharArray();
                this.out.write(enc, start, enc.length);

                // textNode in this context
                textContentList.set(textContentList.size() - 1,
                    new Boolean(true));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endDocument() throws SAXException {
        try {
            this.out.flush();
            this.out.close();

            textContentList.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException {
        try {
            if (parent) {
                this.out.write("/>");
            } else {
                // check for textNodes in this context
                Boolean b = (Boolean) textContentList.remove(textContentList.size() -
                        1);

                if (b.booleanValue()) {
                    this.out.write("</" + qName + ">");
                } else {
                    // there was no textNode we can create a new line
                    this.out.write('\n');
                    indentOutput(indent);
                    this.out.write("</" + qName + ">");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        indent--;
        parent = false;
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
        throws SAXException {
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void startDocument() throws SAXException {
        indent = 0;

        try {
            this.out.write("<?xml version=\"1.0\" encoding=\"" + encoding +
                "\" ?>");

            if (this.dtd != null) {
                this.out.write("\n<!DOCTYPE " + dtd + ">");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startElement(String namespaceURI, String localName,
        String qName, Attributes atts) throws SAXException {
        this.indent++;

        try {
            if (this.parent) {
                // we are nested
                this.out.write(">");
            } else {
                this.parent = true;
            }

            // first create a new line
            this.out.write('\n');

            // indent the line
            this.indentOutput(indent);

            // the element
            this.out.write("<" + qName);

            int attrCount = atts.getLength();

            for (int i = 0; i < attrCount; i++) {
                //we need a white space between the 
                //attributes
                this.indentOutput(1);

                String uri = atts.getURI(i);
                String qname = atts.getQName(i);

                // if (uri.length() > 0) {
                // String prefix = qname.substring(0, qname.indexOf(':'));
                // out
                // .write(" xmlns:" + prefix + "=\"" + uri
                // + "\" ");
                // }
                String value = atts.getValue(i);
                if(value == null){
                	value="";
                }
                this.out.write(qname + "=\"" + encodeXML(atts.getValue(i)) +
                    "\"");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // no text in this context now
        this.textContentList.add(Boolean.valueOf(false));
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
    }

    /**
     * Indent the output
     *
     * @param indentSize
     */
    private void indentOutput(int indentSize) {
        try {
            for (int i = 0; i < indentSize; i++) {
                this.out.write(' ');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String encodeXML(String text) {
        int length = text.length();
        StringBuffer work = new StringBuffer(length);

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (c == '&') {
                work.append("&amp;");
            } else if (c == '<') {
                work.append("&lt;");
            } else if (c == '>') {
                work.append("&gt;");
            } else if (!Character.isIdentifierIgnorable(c)) {
                work.append(c);
            }
        }

        return work.toString();
    }

    public void setDTD(String dtd) {
        this.dtd = dtd;
    }

    protected void queryXMLNS(Attributes atts) {
        for (int i = 0; i < atts.getLength(); i++) {
            String qname = atts.getQName(i);

            if (qname.startsWith("xmlns:")) {
                String prefix = atts.getLocalName(i);
                String uri = atts.getValue(i);
                rootxmlns.put(uri, prefix);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.xml.SAXSerializer#getMimeType()
     */
    public String getMimeType() {
        return MIMETYPE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.xml.SAXSerializer#getSuffix()
     */
    public String getSuffix() {
        if (gzip) {
            return SUFFIX_GZIP;
        } else {
            return SUFFIX;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.xml.SAXSerializer#setOutput(java.io.OutputStream)
     */
    public void setOutput(OutputStream out) {
        OutputStream bout = null;

        try {
            if (gzip) {
                bout = new BufferedOutputStream(new GZIPOutputStream(out));
            } else {
                bout = new BufferedOutputStream(out);
            }

            this.out = new OutputStreamWriter(bout, this.encoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.xml.SAXSerializer#setProperties(java.util.Map)
     */
    public void setProperties(Map properties) {
        this.properties = properties;

        if (properties.containsKey(PROPERTY_ENCODING)) {
            this.encoding = (String) properties.get(PROPERTY_ENCODING);
        }

        if (properties.containsKey(PROPERTY_GZIP)) {
            this.gzip = Boolean.getBoolean((String) properties.get(
                        PROPERTY_GZIP));
        }
    }
}
